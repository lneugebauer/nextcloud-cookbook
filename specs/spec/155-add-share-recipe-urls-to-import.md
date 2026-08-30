# Share recipe URLs into the import screen

Issue: [#155](https://github.com/lneugebauer/nextcloud-cookbook/issues/155) — supersedes PR [#167](https://github.com/lneugebauer/nextcloud-cookbook/pull/167).

## 1. Goals & Requirements

Importing a recipe found in a browser currently means: copy the URL → switch to the app → open the recipe list → tap import → paste. The app should instead appear in the system share sheet so a URL can be sent straight into the existing import flow.

**Functional requirements**

1. The app registers as a share target for `text/plain`, so browsers and other apps can send a URL to it.
2. A shared payload is turned into a URL and imported automatically — the user lands on `DownloadRecipeScreen` with the download already running.
3. Shared text is rarely a bare URL ("Best Lasagna https://example.com/lasagna", a URL plus a newline plus a title). The first `http(s)` URL in the payload is used.
4. If no `http(s)` URL can be extracted, the import screen still opens with the raw shared text prefilled and **no** automatic download, so the user sees what arrived and can correct it.
5. If nothing is imported, the error handling PR #207 added applies unchanged: a server or network failure shows `DownloadRecipeScreenState.Error` with the URL still editable, and an HTTP 409 on an already imported recipe shows the conflict snackbar with *View original*.
6. If the share arrives while logged out, the URL is held and the import screen opens once credentials are available (after the login flow completes).
7. Sharing while the app is already running must not restart it at the splash screen, and must not stack import screens.

**Non-goals**

- Handling `ACTION_SEND` with `EXTRA_STREAM` (images, files) or `ACTION_SEND_MULTIPLE`.
- Registering for `ACTION_VIEW` on `http(s)` (that would make the app a candidate for opening arbitrary web links).
- Direct share targets / sharing shortcuts (Android 10+ `ShortcutManager` API).

## 2. Architecture & Design Decisions

### 2.1 Single activity, intent handling stays in `MainActivity`

The share intent is received by the existing `MainActivity` and funnelled through the existing `MainViewModel.setIntent()` path — no second activity and no XML views. *(Convention: `MainActivity.handleIntent()` / `MainViewModel.setIntent()` already centralise the `nccookbook://` deep-link intent; this is also the approach requested in the PR #167 review.)*

### 2.2 Responsibility split

| Layer | Responsibility |
| --- | --- |
| `MainActivity` | Receives the intent; forwards it once per delivery. |
| `MainViewModel` | Recognises `ACTION_SEND` + `text/plain`, holds the raw shared text as one-shot state, exposes a "handled" callback. |
| `NextcloudCookbookApp` | Decides *when* it is safe to navigate (splash resolved, credentials present) and navigates. |
| `DownloadRecipeViewModel` | Extracts the URL from the payload, seeds the UI state, triggers the import. |

Extraction lives in `DownloadRecipeViewModel` rather than `MainViewModel` because the screen is opened in both cases (URL found or not) and only the screen's state distinguishes them. Keeping it there means the parsing rule has exactly one owner.

### 2.3 URL extraction as a pure Kotlin extension

New `String.extractHttpUrl(): String?` in `core/util/`, implemented with a plain Kotlin `Regex` requiring an explicit `http://` / `https://` scheme, returning the first match with trailing punctuation trimmed. *(Convention: `core/util/StringAddSuffixExtension.kt` + `app/src/test/.../StringAddSuffixUnitTest.kt` — small pure extensions with JVM unit tests.)*

`android.util.Patterns.WEB_URL` (used by PR #167) is deliberately **not** used: it is an Android platform constant that is unavailable in plain JVM unit tests, and it matches scheme-less strings such as `example.com` that the Cookbook API cannot fetch.

Both `http` and `https` are accepted — PR #167 restricted to `https`, which would break self-hosted or LAN recipe sites. Those instances are also frequently reached by IP and port rather than by name (`http://192.168.1.50:8080/...`), so the extension validates nothing about the host form; see §3.2.

### 2.4 The URL travels as a navigation argument

The `DownloadRecipeScreen` **destination** composable gains a nullable `sharedText: String?` navigation argument; `DownloadRecipeViewModel` reads it from `SavedStateHandle["sharedText"]`. PR [#207](https://github.com/lneugebauer/nextcloud-cookbook/pull/207) split the file into the destination composable and a stateless `DownloadRecipeScreenContent`; only the destination declares the argument, and `DownloadRecipeScreenContent` stays free of it. *(Convention: `RecipeDetailScreen` declares `@Suppress("UNUSED_PARAMETER") recipeId: String` and `RecipeDetailViewModel` reads `savedStateHandle["recipeId"]`; `RecipeListViewModel` does the same for `categoryName` / `keyword`.)*

No manual URL encoding is needed. Compose Destinations 2.3.0 encodes `String` nav args when building the route and decodes them on read — verified in the library sources: `DestinationsStringNavType.serializeValue()` calls `encodeForRoute()` (`Uri.encode`), and the generated `argsFrom(savedStateHandle)` reads the decoded value. The manual `URLEncoder`/`URLDecoder` handling and the `"sharedUrl="` string surgery in PR #167 are therefore unnecessary.

The argument is declared **without** a Kotlin default value, and the one existing call site passes `null` explicitly. This mirrors the verified generated code for `RecipeListWithArgumentsScreenDestination(categoryName, keyword)` and avoids depending on default-value propagation through KSP.

It is named `sharedText`, not `url`, because it may legitimately carry non-URL text (requirement 4).

### 2.5 One-shot delivery

`MainViewModel` exposes the shared text as `StateFlow<String?>`, cleared through `onSharedTextHandled()` after navigation. Two duplicate-import hazards are closed:

- **Activity recreation:** `MainActivity.onCreate()` only calls `handleIntent()` when `savedInstanceState == null`. The case this closes is a **process-death restore**: the system recreates the activity and re-delivers the original share intent, and because the `MainViewModel` is gone too, an unguarded `setIntent()` would republish the shared text into a fresh `_sharedTextState` and import the same URL a second time. A plain configuration change is already safe without the guard — the `MainViewModel` survives it with `_sharedTextState` already cleared by `onSharedTextHandled()`.

  The guard does **not** change the existing `nccookbook://` deep-link behaviour. After a rotation the surviving `MainViewModel` still holds the Intent in `_intentState`, so the recreated composition's `LaunchedEffect(intent)` re-runs `navController.handleDeepLink()` on that retained value whether or not `handleIntent()` was called. Suppressing that re-handling would need the intent itself to become one-shot state, which is out of scope here.
- **ViewModel recreation after process death:** `DownloadRecipeViewModel` records that it already fired the automatic import in `SavedStateHandle`, so a restored screen does not import the same URL twice.

### 2.6 Navigation timing: wait for splash and credentials

Navigating as soon as the intent arrives is unsafe. On a cold start the nav host begins at `SplashScreen`, and `SplashScreen` navigates to `HomeScreen` with `popUpTo(SplashScreenDestination) { inclusive = true }` — which would pop an import screen pushed before it. `NextcloudCookbookApp` therefore navigates only when **both** hold:

- `navController.currentDestinationAsState()` reports a destination that is **neither `null` nor `SplashScreenDestination`** (splash has resolved), and
- `LocalCredentials.current != null` (the user is authorised).

The `null` half of the first gate is load-bearing, not defensive: `currentDestinationAsState()` is `currentDestinationFlow.collectAsState(initial = null)`, so it emits `null` on the first composition of a cold start and only reports a real destination once the first back-stack entry arrives. A gate written as "anything other than `SplashScreenDestination`" would therefore pass during that first frame and navigate underneath the splash — the exact race the gate exists to prevent. The check must be `currentDestination != null && currentDestination != SplashScreenDestination`.

`currentDestinationAsState()` is provided by `com.ramcosta.composedestinations.utils` (verified in the 2.3.0 sources); `LocalCredentials` is already provided by `MainActivity` and derived from `MainViewModel.authState`.

Because the shared text is only cleared after a successful navigation, requirement 6 falls out for free: a share received while logged out stays pending, and `MainViewModel.authState` re-emits when `accountRepository.getAccount()` produces an account after login, which fires the effect.

### 2.7 Back-stack handling

- The navigation to the import screen uses `popUpTo(DownloadRecipeScreenDestination) { inclusive = true }` so that sharing twice in a row replaces the import screen instead of stacking two.
- On successful import, the `popUpTo(RecipeListScreenDestination)` inside `DownloadRecipeScreen`'s `onNavigateToDetail` lambda (introduced by #207) changes to `popUpTo(DownloadRecipeScreenDestination) { inclusive = true }`. **This is a required fix, not a cleanup:** entered from a share on a cold start the back stack is `Home → DownloadRecipe`, `RecipeListScreen` is not on it, and `popUpTo` on an absent route is a no-op — the import screen would survive, and returning to it would re-run the `LaunchedEffect(id)` in `DownloadRecipeScreenContent`'s `Loaded` branch and bounce straight back to the recipe detail. Popping the import screen itself produces the identical result for the existing entry point from the recipe list.

### 2.8 `android:launchMode="singleTop"`

With the default `standard` launch mode, a share sent while the app is in the background starts a *second* `MainActivity` on top of the existing task, restarting at the splash screen. `singleTop` delivers the intent to the running instance through the already-implemented `onNewIntent()`. The app is single-activity, so the blast radius is limited to the existing `nccookbook://` deep links, which benefit the same way. *(Research: [Android launch modes](https://developer.android.com/guide/topics/manifest/activity-element#lmode) — `singleTop` reuses the instance when it is at the top of the target task; in a single-activity app it always is.)*

### 2.9 Share-sheet label

The new `<intent-filter>` carries **no** `android:label`, so the share sheet shows the app name and icon — what users scan for. No new string resource is required.

### 2.10 Re-sharing an already imported recipe

No extra work: PR #207 made the ViewModel translate an HTTP 409 into `ConflictState.Active` plus a
reset to `Initial(url)`, which the screen renders as the conflict snackbar with *View original* /
*Dismiss*. Sharing the same page twice — the single most likely repeat action for a share target —
therefore lands on that snackbar instead of a raw error, and the automatic import needs no special
case for it.

### 2.11 ViewModel unit tests

Covered by extending the existing `DownloadRecipeViewModelUnitTest`, which #207 added along with
`testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1'` — so the dependency this
spec previously called for is already on `main` and no `app/build.gradle` change is needed. The file
also fixes the local house style, which differs from the harness in the still-open #209 / #210:
backtick test names, `runTest { }` bodies, an `UnconfinedTestDispatcher` field, and the ViewModel
built in `@Before`. New cases follow **this** file.

`SavedStateHandle` is constructed for real — `SavedStateHandle(mapOf(...))` works on the JVM,
including writes *(convention: `RecipeDetailViewModelUnitTest` in #209 and `BrowserLoginViewModelUnitTest`
in #210 both do this)* — so the nav argument needs no mocking.

## 3. Implementation Changes

### 3.1 `app/src/main/AndroidManifest.xml`

- Add `android:launchMode="singleTop"` to the `.core.presentation.MainActivity` element.
- Add a third `<intent-filter>` to that activity:
  - action `android.intent.action.SEND`
  - category `android.intent.category.DEFAULT`
  - data `android:mimeType="text/plain"`

### 3.2 New: `core/util/StringExtractHttpUrlExtension.kt`

`fun String.extractHttpUrl(): String?`

- Matches the first `http://` or `https://` token, case-insensitively, terminated by whitespace.
- Strips trailing punctuation that commonly abuts a shared URL: `. , ; : ! ? > " '` unconditionally,
  and a trailing `)`, `]` or `}` only while the match holds more of that closing bracket than of its
  opener.
- Returns `null` when nothing matches or when the remainder after the scheme is empty.

The bracket balancing is not gold-plating; two required inputs disagree about the same character.
`(https://example.com/recipe)` matches from after the `(`, so the token carries one unmatched `)` that
must go — while `http://[fd00::1]` is an IPv6 literal that carries its own `[`, so its `]` must stay.
Stripping blindly would hand the API `http://[fd00::1`. The same rule incidentally keeps
`https://en.wikipedia.org/wiki/Lasagne_(dish)` intact.

**Host form is not validated.** What follows the scheme may be a domain, a `.local` name, a bare IPv4
address, or a bracketed IPv6 literal, each with an optional `:port` — self-hosted users commonly reach
their instance as `http://192.168.1.50:8080/...`. The extension parses none of it and asserts nothing
about it; the server decides what it can fetch. The `:` before a port survives because only *trailing*
punctuation is stripped.

### 3.3 `core/presentation/MainViewModel.kt`

- Add `private val _sharedTextState = MutableStateFlow<String?>(null)` and its public `StateFlow<String?>`.
- In `setIntent()`, after the existing `_intentState` update: when `intent.action == Intent.ACTION_SEND` and `intent.type == "text/plain"`, read `Intent.EXTRA_TEXT`, trim it, and publish it if it is not blank.

  Read it as `intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()`, **not** `getStringExtra()`. `EXTRA_TEXT` is documented as a `CharSequence`, and apps that share styled text put a `Spanned` in it; `Bundle.getString()` casts and swallows the resulting `ClassCastException`, so `getStringExtra()` silently returns `null` and the share is dropped for exactly those senders.
- Add `fun onSharedTextHandled()` clearing `_sharedTextState`.

`_intentState` keeps carrying the raw intent — the existing `navController.handleDeepLink()` path needs it untouched.

### 3.4 `core/presentation/MainActivity.kt`

- `onCreate`: guard the `handleIntent(intent)` call with `if (savedInstanceState == null)`. `onNewIntent` keeps calling it unconditionally.
- In `setContent`, collect `viewModel.sharedTextState` and pass it — together with `viewModel::onSharedTextHandled` — into `NextcloudCookbookApp`.
- `NextcloudCookbookApp` signature becomes `(intent: Intent?, sharedText: String?, onSharedTextHandled: () -> Unit)`.
- Inside `NextcloudCookbookApp`, next to the existing deep-link `LaunchedEffect`:
  - obtain `val destinationsNavigator = navController.rememberDestinationsNavigator()` (as `BottomBar.kt` does) and `val currentDestination by navController.currentDestinationAsState()`;
  - a `LaunchedEffect` keyed on `sharedText`, `currentDestination` and `LocalCredentials.current` that returns early unless all gates of §2.6 pass — non-null `sharedText`, `currentDestination != null`, `currentDestination != SplashScreenDestination`, non-null credentials — then navigates to `DownloadRecipeScreenDestination(sharedText = sharedText)` with `popUpTo(DownloadRecipeScreenDestination) { inclusive = true }`, and finally invokes `onSharedTextHandled()`.

### 3.5 `recipe/presentation/download/DownloadRecipeScreen.kt`

- Add the parameter `@Suppress("UNUSED_PARAMETER") sharedText: String?` (no default) between `navigator` and `viewModel`; the value is consumed by the ViewModel via `SavedStateHandle`.
- In the `onNavigateToDetail` lambda passed to `DownloadRecipeScreenContent`, change the nav options from `popUpTo(RecipeListScreenDestination)` to `popUpTo(DownloadRecipeScreenDestination) { inclusive = true }` (§2.7), and drop the now-unused `RecipeListScreenDestination` import.
- `DownloadRecipeScreenContent`, `DownloadRecipeForm`, the conflict wiring and the `@Preview` are untouched.

### 3.6 `recipe/presentation/download/DownloadRecipeViewModel.kt`

- Add `savedStateHandle: SavedStateHandle` after `recipeRepository` — the project orders constructor parameters alphabetically (`RecipeDetailViewModel`, `RecipeListViewModel`).
- Add an `init` block:
  - read `savedStateHandle.get<String>("sharedText")`; do nothing when null/blank;
  - `val url = sharedText.extractHttpUrl()`;
  - seed `_uiState` with `DownloadRecipeScreenState.Initial(url = url ?: sharedText)`;
  - when `url != null` **and** `savedStateHandle.get<Boolean>("autoImportTriggered")` is not `true`: write the flag back to the `SavedStateHandle` and call `importRecipe()`.
- `importRecipe()`, `handleConflict()`, `dismissConflict()` and the `_conflict` flow stay exactly as #207 left them. The `init` block reaches `importRecipe()` through its normal path, so a 409 on a re-shared URL produces the conflict snackbar for free (§2.10).

Ordering matters in the `init` block: seed `_uiState` **before** calling `importRecipe()`, because `importRecipe()` returns early unless the state is `Initial` with the URL already in it.

### 3.7 `recipe/presentation/list/RecipeListScreen.kt`

- Line ~147: `onImportClick = { navigator.navigate(DownloadRecipeScreenDestination(sharedText = null)) }`.

### 3.8 Not changed

No API, DTO, repository, database or string-resource changes, no translation work, and no `app/build.gradle` change — #207 already brought `kotlinx-coroutines-test`. `RecipeRepository.importRecipe(ImportUrlDto)`, `DownloadRecipeScreenState`, `ConflictState` and `ConflictSnackbar` are reused as-is. `ScreenshotsTestSuite` is untouched (see §4.4).

## 4. Test Cases

### 4.1 `app/src/test/.../StringExtractHttpUrlUnitTest.kt` (new)

JUnit 4, no mocks, no new dependencies — same shape as `StringAddSuffixUnitTest`.

| Input | Expected |
| --- | --- |
| `"https://example.com/recipe"` | `"https://example.com/recipe"` |
| `"http://cookbook.local/recipe"` | `"http://cookbook.local/recipe"` |
| `"http://192.168.1.50/recipe"` | `"http://192.168.1.50/recipe"` (bare IPv4 host) |
| `"http://192.168.1.50:8080/recipe"` | `"http://192.168.1.50:8080/recipe"` (port preserved — the `:` is not trailing) |
| `"http://192.168.1.50:8080"` | `"http://192.168.1.50:8080"` (host and port, no path) |
| `"Recipe on the NAS http://192.168.1.50:8080/r/42."` | `"http://192.168.1.50:8080/r/42"` (trailing dot trimmed, port intact) |
| `"https://[fd00::1]:8080/recipe"` | `"https://[fd00::1]:8080/recipe"` (bracketed IPv6 literal) |
| `"http://[fd00::1]"` | `"http://[fd00::1]"` (the `]` closes the literal, it is not trailing punctuation) |
| `"Best Lasagna https://example.com/lasagna"` | `"https://example.com/lasagna"` |
| `"https://example.com/a\nCheck this out"` | `"https://example.com/a"` |
| `"https://a.example.com/x https://b.example.com/y"` | `"https://a.example.com/x"` |
| `"https://example.com/r?portion=4&unit=g"` | `"https://example.com/r?portion=4&unit=g"` (query preserved) |
| `"Look at https://example.com/recipe."` | `"https://example.com/recipe"` (trailing dot trimmed) |
| `"(https://example.com/recipe)"` | `"https://example.com/recipe"` |
| `"https://en.wikipedia.org/wiki/Lasagne_(dish)"` | `"https://en.wikipedia.org/wiki/Lasagne_(dish)"` (balanced parens kept) |
| `"HTTPS://EXAMPLE.COM/R"` | `"HTTPS://EXAMPLE.COM/R"` (case-insensitive match, value untouched) |
| `"example.com/recipe"` | `null` (no scheme) |
| `"192.168.1.50:8080/recipe"` | `null` (no scheme — an IP is not special-cased) |
| `"Some lovely recipe"` | `null` |
| `""` | `null` |
| `"https://"` | `null` |

### 4.2 `app/src/test/.../recipe/presentation/download/DownloadRecipeViewModelUnitTest.kt` (extend)

PR #207 created this file with four cases (conflict, success, generic error, dismiss). **Extend it —
do not add a second file.** Two adjustments to what is already there:

- `setUp()` currently builds `DownloadRecipeViewModel(recipeRepository)`. It gains an empty
  `SavedStateHandle()`, which keeps all four existing cases on the manual-entry path (no
  `sharedText`, no auto-import) and so leaves their assertions untouched.
- The new cases cannot reuse that instance, because the automatic import runs in `init`. Add a
  `createViewModel(savedStateHandle: SavedStateHandle)` helper and build per case. **Stub the
  repository before constructing**, otherwise the `init` import runs against an unstubbed mock —
  this is the one ordering trap in the file.

Style follows the existing file, not #209 / #210: backtick test names, `runTest { }` bodies, the
`UnconfinedTestDispatcher` field. The unconfined dispatcher drains `init` during construction, so
each case is a synchronous assertion on `viewModel.uiState.value` afterwards.

New cases:

| Setup | Action | Expectation |
| --- | --- | --- |
| `sharedText` = `"https://example.com/r"`, repository returns `Resource.Success(emptyRecipeDto().copy(id = "42"))` | construct the ViewModel | `uiState` is `Loaded(id = "42")`; `importRecipe` called once with `ImportUrlDto("https://example.com/r")` |
| `sharedText` = `"Title https://example.com/r"` | construct the ViewModel | repository called with `ImportUrlDto("https://example.com/r")` — the title is not sent |
| `sharedText` = `"just some text"` | construct the ViewModel | `uiState` is `Initial(url = "just some text")`; `verifyNoInteractions(recipeRepository)` |
| empty `SavedStateHandle()` | construct the ViewModel | `uiState` is `Initial(url = "")`; repository never called — the manual-entry path is unchanged |
| `sharedText` = `"https://example.com/r"`, `"autoImportTriggered"` already `true` | construct the ViewModel | `uiState` is `Initial(url = "https://example.com/r")`; repository never called — the process-death guard of §2.5 |
| `sharedText` = `"https://example.com/r"` | construct the ViewModel | `savedStateHandle.get<Boolean>("autoImportTriggered")` is `true` afterwards — the flag is persisted, not just held in memory |
| `sharedText` = `"https://example.com/r"`, repository returns `Resource.Error` | construct the ViewModel | `uiState` is `Error(url = "https://example.com/r", …)` — the URL stays editable for a retry |
| `sharedText` = `"https://example.com/r"`, repository returns the 409 conflict resource (reuse the file's `conflictResource` helper) | construct the ViewModel | `conflict` is `ConflictState.Active`; `uiState` is `Initial(url = "https://example.com/r")` — the re-share path of §2.10 |

### 4.3 Manual verification

`MainViewModel`'s intent branch and the navigation gating both need a real `Intent` and a live nav host, so they are verified by hand rather than by unit test (adding Robolectric for two branches is not worth it).

Send a share intent to the debug build (application id `de.lukasneugebauer.nextcloudcookbook.debug`):

```
adb shell am start -a android.intent.action.SEND -t text/plain \
  --es android.intent.extra.TEXT "https://example.com/recipe" \
  -n de.lukasneugebauer.nextcloudcookbook.debug/de.lukasneugebauer.nextcloudcookbook.core.presentation.MainActivity
```

Cases to walk through:

1. **App not running, logged in** → splash, then the import screen with the download already running, then the recipe detail. Back → home, *not* the import screen.
2. **App running in foreground on the recipe list** → no splash, no second app instance; the import screen opens on top. Back after the import → recipe list.
3. **Logged out** → the login flow appears; after signing in, the import screen opens with the shared URL and imports.
4. **Rotate the device while the import screen is loading** → exactly one import request (check the server or the Timber log), no second navigation.
5. **Process death while the import screen is open** — share a URL, background the app, kill it with `adb shell am kill de.lukasneugebauer.nextcloudcookbook.debug`, then reopen it from recents → the import screen is restored *without* firing a second import. This is the case the `savedInstanceState` guard of §2.5 and the `autoImportTriggered` flag of §3.6 exist for; a rotation does not exercise either.
6. **Share twice in a row with different URLs** → one import screen at a time, showing the second URL.
7. **Share a non-URL** (`--es android.intent.extra.TEXT "just some text"`) → the import screen opens with the text prefilled and no download started.
8. **Share a URL the server cannot parse** → the error state renders with the URL still editable and the retry button working.
9. **Share a recipe that is already in the library** → the automatic import gets an HTTP 409 and the conflict snackbar appears with the recipe name; *View original* opens it (§2.10).
10. **Real share sheet** → the app appears with its own name and icon when sharing a page from a browser.

### 4.4 Build checks

`./gradlew ktlintCheck test lint` — the four pre-existing `DownloadRecipeViewModelUnitTest` cases
must still pass, since the ViewModel gains a constructor parameter and an `init` block. Plus
`./gradlew compileFullDebugAndroidTestKotlin` — CI never
compiles the `androidTest` source set, so a break there is easy to miss. `ScreenshotsTestSuite` only
instantiates layout composables (`RecipeDetailLayout`, `HomeScreen`, `StartLayout`,
`ManualLoginLayout`, `BottomBarContent`) and never `DownloadRecipeScreen`, so the new navigation
argument should not reach it — the compile check is what confirms that.
