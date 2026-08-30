# Task 1 — Sign in through a Custom Tab (replaces the WebView)

**Spec:** `specs/spec/183-fix-passkey-login.md` — read §1, §2.2, §2.3, §2.5, §2.7, §2.8, §3.1–§3.10,
§4.1 (cases 1, 2, 5, 6, 7, 8, 9), §4.2, and §4.3 steps 1–4, 6, 8.
**Issue:** [#183 — Unable to login using Authentik Passkey (WebAuthn)](https://github.com/lneugebauer/nextcloud-cookbook/issues/183)

**Dependencies:** none. This is the first task.

**In-flight overlap:** [PR 209](https://github.com/lneugebauer/nextcloud-cookbook/pull/209) touches
`app/build.gradle` and `ScreenshotsTestSuite.kt` — see step 1 and step 8 below.

## Goal

A user whose Nextcloud (or its upstream identity provider) requires a passkey can sign in. The same
applies to hardware security keys, browser password managers, client certificates, and existing SSO
sessions — everything the WebView blocks or degrades.

**Root cause, already verified in the spec:** `WebViewLoginScreen.kt:98`–`:105` loads the login URL
into a bare `WebView` with only `settings.javaScriptEnabled = true`. WebAuthn is off by default in a
WebView (`WEB_AUTHENTICATION_SUPPORT_NONE`), so `navigator.credentials.get()` rejects inside the page
and the identity provider renders its generic "Authentication failed".

**Do not try to enable WebAuthn in the WebView.** Spec §2.1 rules it out with sources: the app-facing
support level requires a Digital Asset Links association hosted on each user's own server domain, the
browser-facing one requires being on Google's privileged-browser allowlist, and the documented setup
pulls `androidx.credentials:credentials-play-services-auth`, which the F-Droid `full` flavour cannot
ship. The fix is to stop using a WebView for sign-in.

Nothing about the Login Flow v2 protocol handling changes: the app still POSTs to
`/index.php/login/v2` and polls the poll endpoint. Only the transport for the *login page* changes.

## What to implement

### 1. `app/build.gradle`

Add next to the other `androidx.*` entries:

```groovy
implementation 'androidx.browser:browser:1.10.0'
```

Add next to the existing `testImplementation` entries — **but check first**, PR 209 adds this exact
line immediately after `mockito-core`. If it is already there, confirm the version is `1.10.2` and
leave it:

```groovy
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2'
```

`androidx.browser:browser:1.10.0` requires `minSdk 23`; this app is on `minSdk 25`
(`app/build.gradle:39`). It pulls no Play Services. `aboutlibraries` picks the new dependency up at
build time — no manual licence entry.

### 2. New — `core/util/UriOpenInCustomTabExtension.kt`

`app/src/main/java/de/lukasneugebauer/nextcloudcookbook/core/util/UriOpenInCustomTabExtension.kt`

Sits beside the existing `UriOpenInBrowserExtension.kt` and follows its `Uri`-receiver shape.

```kotlin
fun Uri.openInCustomTab(context: Context): Boolean =
    try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, this)
        true
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No browser available to open the login page")
        false
    }
```

Two constraints that are easy to get wrong:

- **Do not pre-check with `Intent.resolveActivity`.** Android 11 package visibility makes it return
  `null` without a `<queries>` declaration, so it would report "no browser" on every modern device.
  Catch `ActivityNotFoundException` instead. This is also why **`AndroidManifest.xml` needs no
  changes** — implicit `ACTION_VIEW` launches are not filtered, only *queries* are (spec §2.7). The
  app already relies on this: `Uri.openInBrowser` works from the Settings and Info screens today with
  no `<queries>` present.
- **The caller must pass the Activity context.** `LocalContext.current` inside `setContent` is the
  Activity's context wrapper, which is what puts the Custom Tab in the app's own task. An application
  context would force `FLAG_ACTIVITY_NEW_TASK`, put the tab in a separate task, and break task 2.

### 3. `auth/domain/state/WebViewScreenState.kt` → `BrowserLoginScreenState.kt`

Rename the file and the interface. `pollLoginServerIsActive` goes away — `Loaded` is now the only
polling state, so the guard becomes `_uiState.value is Loaded`. `browserLaunched` replaces it as a
one-shot guard so the tab opens exactly once per `Loaded` state.

```kotlin
sealed interface BrowserLoginScreenState {
    object Initial : BrowserLoginScreenState

    data class Loaded(
        val loginUrl: Uri,
        val browserLaunched: Boolean = false,
    ) : BrowserLoginScreenState

    object Authenticated : BrowserLoginScreenState

    data class Error(val uiText: UiText) : BrowserLoginScreenState
}
```

Sealed interfaces for state are the established pattern here — match the shape of the sibling files
in `auth/domain/state/`.

### 4. New — `auth/presentation/browser/BrowserLoginViewModel.kt`

A rename-and-trim of `WebViewLoginViewModel`. **Copy `observeAuthorizationStatus()` (`:93`–`121`) and
`pollLoginServer()` (`:124`–`141`) across unchanged**, along with `POLL_DELAY = 5_000L`, the
`@HiltViewModel` annotation, and the full constructor injection list (`accountRepository`,
`authRepository`, `clearPreferencesUseCase`, `ncCookbookApiProvider`, `preferencesManager`,
`savedStateHandle`).

Changes relative to the old file:

| Change | Detail |
| --- | --- |
| Remove | `onWebViewLoadError` (`:52`–`62`) — no WebView left to report load errors |
| Add | `fun onBrowserLaunched()` — sets `Loaded.browserLaunched = true` |
| Add | `fun onNoBrowserAvailable()` — `Error(UiText.StringResource(R.string.error_no_browser))` |
| Add | `fun onOpenBrowserClick()` — sets `browserLaunched = false`, so the §2.5 collector re-launches the tab |
| Change | Polling guard becomes `_uiState.value is BrowserLoginScreenState.Loaded` |

`init` keeps its current behaviour: read `savedStateHandle["url"]`, call `getLoginEndpoint(url)` and
`observeAuthorizationStatus()`, or emit `Error(R.string.error_invalid_url)` when the URL is missing.
**Persisting the poll token is task 3 — do not add it here.**

### 5. New — `auth/presentation/browser/BrowserLoginScreen.kt`

Replaces `WebViewLoginScreen.kt`. Keep `@Destination<MainGraph>`, the
`AnimatedVisibilityScope.` receiver, `HideBottomNavigation()`, the `Scaffold` with the back-arrow
`TopAppBar`, `Loader`, and `AbstractErrorScreen` with `onRetryClick`.

**Signature: `url: String` only.** Drop `allowSelfSignedCertificates` — it existed solely to
configure `WebViewClient`. The preference is persisted independently of navigation
(`StartScreenViewModel.kt:29` and `:42`–`:43`) and consumed by `OkHttpClientProvider`, so the app's
own HTTP calls are unaffected.

Compose Destinations generates `BrowserLoginScreenDestination` from the composable name.

State rendering:

| State | UI |
| --- | --- |
| `Initial` | `Loader` |
| `Loaded` | New private `BrowserLoginLayout` (below) |
| `Authenticated` | `Loader` — the collector is navigating |
| `Error` | `AbstractErrorScreen(uiText, onRetryClick = viewModel::retry)` |

`BrowserLoginLayout` is a centred `Column` with `Text(R.string.login_browser_waiting)`, a `Loader`,
and a `DefaultTextButton(R.string.login_browser_open)`. Follow `StartLayout`
(`StartScreen.kt:126`–`:214`) for structure: `Modifier.fillMaxSize()`, `Arrangement.Center`,
`Alignment.CenterHorizontally`, `verticalScroll(rememberScrollState())` so it survives landscape on a
short screen, `dimensionResource` for spacing, and `Modifier.padding(innerPadding)` from the
`Scaffold`. Add a `@Preview` for it matching `StartLayoutPreview` (`StartScreen.kt:236`) — CI
compiles previews, since `assemble` builds `FullRelease`.

**The side effect — this is the part that is easy to get wrong.** Use a single
`LaunchedEffect(Unit)` that collects the flow, **not** the `LaunchedEffect(uiState)` pattern used
elsewhere in this codebase (`WebViewLoginScreen.kt:48`):

```kotlin
val context = LocalContext.current
LaunchedEffect(Unit) {
    viewModel.uiState.collect { state ->
        when (state) {
            is BrowserLoginScreenState.Loaded ->
                if (!state.browserLaunched) {
                    if (state.loginUrl.openInCustomTab(context)) {
                        viewModel.onBrowserLaunched()
                    } else {
                        viewModel.onNoBrowserAvailable()
                    }
                }
            BrowserLoginScreenState.Authenticated ->
                navigator.navigate(HomeScreenDestination) {
                    popUpTo(StartScreenDestination) { inclusive = true }
                }
            else -> Unit
        }
    }
}
```

Why it must be shaped this way: while the Custom Tab is in front, `MainActivity` is stopped, and on
`ON_STOP` `WindowRecomposer` calls `recomposer.pauseCompositionFrameClock()`
(`WindowRecomposer.android.kt:410`–`414`), which pauses the `withFrameNanos` that
`Recomposer.runRecomposeAndApplyChanges` awaits before applying changes (`Recomposer.kt:609`). A
state-keyed `LaunchedEffect` therefore may not re-fire until the user returns. A coroutine already
suspended inside a `LaunchedEffect` is not affected, because `AndroidUiDispatcher.dispatch` posts the
resumption to the main-thread `Handler` as well as scheduling a frame callback
(`AndroidUiDispatcher.android.kt:133`–`144`), and the handler message is delivered regardless of
frames. Task 2 depends on this property, so getting it right here matters beyond this task.

### 6. `auth/domain/state/StartScreenState.kt` and `auth/presentation/start/StartScreen.kt`

| Location | Change |
| --- | --- |
| `StartScreenState.kt:19` | `object WebView` → `object SignIn` — the event expresses the user's intent, not the transport |
| `StartScreen.kt:42` | import `BrowserLoginScreenDestination` instead of `WebViewLoginScreenDestination` |
| `StartScreen.kt:71`–`79` | `StartScreenSignInEvent.SignIn -> navigator.navigate(BrowserLoginScreenDestination(url = data.url))` — no `allowSelfSignedCertificates` argument |
| `StartScreen.kt:111` | `onSignInClick = { viewModel.onLoginClick(event = StartScreenSignInEvent.SignIn) }` |
| `StartScreen.kt:131`, `:179`, `:197`, `:242` | rename the `onWebViewLoginClick` parameter to `onSignInClick` |

`StartScreenViewModel` is unchanged.

### 7. `app/src/main/res/values/strings.xml`

Add, keeping the file's alphabetical ordering:

```xml
<string name="error_no_browser">No browser app found. Install a browser or use manual sign in.</string>
<string name="login_browser_open">Open browser</string>
<string name="login_browser_waiting">Complete the sign in in your browser. This screen updates automatically.</string>
```

Remove `error_webview_load_failed` (`:66`) — its only caller was `onWebViewLoadError`.

**Edit only `values/strings.xml`.** Translations in `values-*/strings.xml` are Weblate-managed; let
Weblate reconcile the removal.

### 8. `app/src/androidTest/…/screenshots/ScreenshotsTestSuite.kt`

Rename `onWebViewLoginClick = {}` → `onSignInClick = {}` in the `startScreen()` case (currently
`:68`). **Find it by name** — PR 209 edits the `recipeDetailScreen()` case further down and will
shift line numbers if it lands first.

This is required: without it the `androidTest` source set stops compiling. CI never compiles it, so
verify locally (see Verification).

Optionally add a `browserLoginScreen()` case rendering `BrowserLoginLayout`, mirroring
`manualLoginScreen()` at `:76`. Not required — skip unless store screenshots are being refreshed.

### 9. Deletions

- `auth/presentation/webview/WebViewLoginScreen.kt`
- `auth/presentation/webview/WebViewLoginViewModel.kt`
- `auth/domain/WebViewClient.kt` — `WebViewLoginScreen.kt:101` is its only usage

### 10. `docs/faqs.md`

Add after "Can I connect via http?", following the file's badge convention:

> **## Why does sign in open my browser?** `<Badge type="tip" text="^0.31.0" />`
>
> Nextcloud's recommended sign-in flow runs in your default browser so that passkeys, security keys,
> password managers, and single sign-on providers all work. The app never sees your password — it
> receives an app-specific token once the browser sign-in completes.
>
> **Self-signed certificates.** Keep ticking "Allow self-signed certificates" — the app still needs
> it. Your browser will additionally show its own certificate warning during sign in; tap
> *Advanced → Proceed*. Passkeys cannot be used with a self-signed certificate: browsers block
> WebAuthn on sites with certificate errors. To get both, install your server's certificate on the
> device (*Settings → Security → Encryption & credentials → Install a certificate → CA certificate*),
> which removes the warning entirely. Otherwise sign in with a password, or use **Manual sign in**
> with an app password created in Nextcloud under *Settings → Security*.

### 11. New — `BrowserLoginViewModelUnitTest.kt`

`app/src/test/java/de/lukasneugebauer/nextcloudcookbook/auth/presentation/browser/BrowserLoginViewModelUnitTest.kt`

No ViewModel test for this class exists. Follow `SyncRecipesUseCaseUnitTest` — plain `mock()` fields
assigned in `@Before`, not `@Mock` + `MockitoAnnotations.openMocks`.

Fixture (all signatures verified against source):

| Collaborator | Setup |
| --- | --- |
| `authRepository` | `mock()`; `getLoginEndpoint(any())` → `Resource.Success(LOGIN_ENDPOINT)` |
| `accountRepository` | `mock()`; `getAccount()` returns `Flow<Resource<NcAccount>>` — stub with `flowOf(Resource.Error(…))` so `observeAuthorizationStatus` stays inert unless a test drives it |
| `ncCookbookApiProvider` | `mock()`; `apiFlow` is a `StateFlow<NcCookbookApi?>` — stub with `MutableStateFlow(null)` |
| `preferencesManager` | `mock()` |
| `clearPreferencesUseCase` | `mock()` — it is a `suspend operator fun invoke()` |
| `savedStateHandle` | real `SavedStateHandle(mapOf("url" to "https://cloud.example.tld"))` — the `@VisibleForTesting` map constructor, no `Bundle` involved (`testOptions.unitTests.returnDefaultValues` is **not** enabled in this project) |

```kotlin
// Uri is an Android framework type; mock it rather than calling Uri.parse on the JVM.
private val LOGIN_URI = mock<Uri>().apply {
    whenever(toString()).thenReturn("https://cloud.example.tld/index.php/login/v2/flow/abc")
}

val LOGIN_ENDPOINT = LoginEndpointResult(
    token = "token-1",
    pollUrl = "https://cloud.example.tld/index.php/login/v2/poll",
    loginUrl = LOGIN_URI,
)

val LOGIN_RESULT = LoginResult(
    ncAccount = NcAccount(
        name = "Test",
        username = "test",
        token = "app-password",
        url = "https://cloud.example.tld",
    ),
)
```

`LOGIN_RESULT` is required, not optional: `pollLoginServer` dereferences `result.data?.ncAccount!!`.
The `NcAccount` shape is copied from the existing fixture at `RecipeRepositoryImplUnitTest.kt:772`.

`@Before` sets `Dispatchers.setMain(StandardTestDispatcher())`; `@After` calls
`Dispatchers.resetMain()`. Use `StandardTestDispatcher` (not `Unconfined`) — the retry case needs
`advanceTimeBy` over the 5 s `POLL_DELAY`. `runTest` adopts the scheduler of the `TestDispatcher`
installed via `setMain`, so virtual time is shared without passing a scheduler around; construct the
ViewModel inside each `runTest` body so `init` runs under it.

Cases to write (spec §4.1 numbering — 3, 4, 10, 11 belong to task 3, skip them here):

1. `init_withValidUrl_requestsLoginEndpointAndEntersLoaded` — `verify(authRepository).getLoginEndpoint("https://cloud.example.tld")`; state is `Loaded` with `browserLaunched == false`.
2. `init_withoutUrl_entersErrorState` — fixture `SavedStateHandle(emptyMap())`; state is `Error` with `R.string.error_invalid_url`; `authRepository` never touched.
5. `onBrowserLaunched_setsBrowserLaunchedFlag` — guards against reopening the tab on every emission.
6. `onOpenBrowserClick_clearsBrowserLaunchedFlag` — act `onBrowserLaunched()` then `onOpenBrowserClick()`; this is what makes "Open browser" re-launch the tab.
7. `onNoBrowserAvailable_entersErrorState` — `Error` with `R.string.error_no_browser`.
8. `pollLoginServer_onError_retriesAfterPollDelay` — `tryLogin` returns `Resource.Error` then `Resource.Success(LOGIN_RESULT)`; `advanceTimeBy(POLL_DELAY + 1)`; assert `times(2)` on `tryLogin`, plus `verify(preferencesManager).updateNextcloudAccount(any())` and `verify(ncCookbookApiProvider).initApi()`.
9. `pollLoginServer_stopsWhenStateLeavesLoaded` — drive the state to `Error` via `onNoBrowserAvailable()`, `advanceTimeBy(POLL_DELAY * 3)`, assert `tryLogin` invoked exactly once. This proves the `is Loaded` guard replacing `pollLoginServerIsActive` still stops the recursion.

## Out of scope for this task

- The bring-to-front `startActivity` on success — that is
  [`task-2-return-to-app-on-success.md`](task-2-return-to-app-on-success.md). After this task, the
  Custom Tab stays on top after a successful sign-in and the user presses Back to reach the app,
  which is already on Home. That is the expected intermediate behaviour, not a bug.
- Persisting the poll token across process death — that is
  [`task-3-survive-process-death.md`](task-3-survive-process-death.md).
- Everything in the spec's §1 "Out of scope".

## Verification

Automated:

1. `./gradlew ktlintFormat` then `./gradlew ktlintCheck` — runs before `test` in CI, no kapt, works
   in place.
2. `./gradlew testFullDebugUnitTest` — the seven new cases pass. Note there is no
   `testDebugUnitTest` task — flavors.
3. `./gradlew compileFullDebugAndroidTestKotlin` — proves the `ScreenshotsTestSuite` rename is
   complete. CI never compiles androidTest, so this only fails later, in the `screenshots` lane.
4. `./gradlew assembleFullRelease` (or `bundle exec fastlane build`) — confirms the `@Preview`
   composables compile.

Manual, on a device (spec §4.3):

5. **The reported case (step 1).** Nextcloud fronted by Authentik with a passkey enrolled. Enter the
   server URL → **Sign in**. A Custom Tab opens; choose "Login with Passkey"; the Android passkey
   sheet appears; authenticate. Press Back → the app is on Home, signed in. *This is #183.*
6. **Password + 2FA (step 2)** on a plain Nextcloud — unchanged behaviour.
7. **Security key (step 3)** with a USB/NFC FIDO2 key.
8. **Self-signed certificate (step 4).** Untrusted certificate, "Allow self-signed certificates"
   ticked: the browser's interstitial offers *Advanced → Proceed*, proceeding loads the login page,
   password sign-in completes, and the recipe list then loads over the trust-all client. Confirm
   **Manual sign in** with an app password still works too. (The interstitial-override half was
   already verified standalone against `https://self-signed.badssl.com/` — spec §2.3.)
9. **Tab dismissed mid-flow (step 6).** Swipe the Custom Tab away before authenticating → the app
   shows "Complete the sign in in your browser"; **Open browser** re-opens the same login URL and
   finishing there still signs in on the same token.
10. **Back out (step 8).** Press Back from the Custom Tab without signing in → the sign-in screen,
    then Back again → the start screen. No crash, no orphaned polling.
11. **No browser installed** (emulator with the browser disabled) → the error screen shows
    `error_no_browser` rather than crashing.

## Done when

- Passkey sign-in completes against a real Authentik-backed Nextcloud (verification step 5).
- The seven unit cases pass and `compileFullDebugAndroidTestKotlin` succeeds.
- `auth/presentation/webview/` and `auth/domain/WebViewClient.kt` no longer exist, and nothing
  references `WebViewScreenState`, `onWebViewLoginClick`, or `error_webview_load_failed`.
- `AndroidManifest.xml` is unchanged.

## Commit

Conventional-commit `fix:` subject describing the user-visible behaviour, e.g.
`fix: sign in through the browser so passkeys work`. Reference #183 in the pull request, not the
commit body — recent history does not put issue refs in commit bodies. No `CHANGELOG.md`, no
`fastlane/metadata` changelog edits.
