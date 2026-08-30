# Task 3 — Share target registration and gated navigation

**Spec:** [`specs/spec/155-add-share-recipe-urls-to-import.md`](../../spec/155-add-share-recipe-urls-to-import.md) — §2.1, §2.5, §2.6, §2.8, §2.9, §3.1, §3.3, §3.4, §4.3

**Dependencies:** [Task 2](task-2-shared-text-nav-argument.md) must be merged first — this task
navigates to `DownloadRecipeScreenDestination(sharedText = …)`, which only accepts an argument after
task 2. Task 2 in turn depends on [Task 1](task-1-extract-http-url.md).

## Goal

Make the app appear in the system share sheet for `text/plain`, and deliver the shared payload into
the import screen at a moment when navigating is actually safe. This is the task that makes the
feature reachable: everything task 2 built is currently dormant behind a `null` argument.

## What to implement

### 1. `app/src/main/AndroidManifest.xml` (§3.1, §2.8, §2.9)

Add `android:launchMode="singleTop"` to the `.core.presentation.MainActivity` element, and a third
`<intent-filter>` to it:

- action `android.intent.action.SEND`
- category `android.intent.category.DEFAULT`
- data `android:mimeType="text/plain"`

`singleTop` is required, not a nicety. With the default `standard` launch mode a share sent while the
app is in the background starts a *second* `MainActivity` on top of the existing task and restarts at
the splash screen. `singleTop` delivers the intent to the running instance through the already
implemented `onNewIntent()`. The app is single-activity, so the blast radius is limited to the
existing `nccookbook://` deep links, which benefit the same way.

Give the new filter **no** `android:label`, so the share sheet shows the app name and icon — what
users actually scan for. No new string resource, and therefore no translation work.

Do **not** add `ACTION_SEND_MULTIPLE`, an `EXTRA_STREAM`/image filter, or an `ACTION_VIEW` filter on
`http(s)`. All three are explicit non-goals; the last would make the app a candidate for opening
arbitrary web links.

### 2. `MainViewModel.kt` (§3.3)

- Add `private val _sharedTextState = MutableStateFlow<String?>(null)` and its public
  `StateFlow<String?>`, following the existing `_intentState` / `intentState` pair directly above it.
- In `setIntent()`, after the existing `_intentState` update: when `intent.action == Intent.ACTION_SEND`
  and `intent.type == "text/plain"`, read the extra, trim it, and publish it only if it is not blank.
- Add `fun onSharedTextHandled()` clearing `_sharedTextState`.

Read the extra as `intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()`, **not**
`getStringExtra()`. `EXTRA_TEXT` is documented as a `CharSequence`, and apps that share styled text
put a `Spanned` in it; `Bundle.getString()` casts and swallows the resulting `ClassCastException`, so
`getStringExtra()` silently returns `null` and the share is dropped for exactly those senders.

Leave `_intentState` carrying the raw intent unchanged — the existing `navController.handleDeepLink()`
path needs it untouched. The share intent flows through both, but `intent.data` is `null` for
`ACTION_SEND`, so the deep-link `LaunchedEffect` returns early on its own.

### 3. `MainActivity.kt` (§3.4, §2.5, §2.6)

**`onCreate`:** guard the `handleIntent(intent)` call with `if (savedInstanceState == null)`.
`onNewIntent` keeps calling it unconditionally.

The case this closes is a **process-death restore**: the system recreates the activity and re-delivers
the original share intent, and because the `MainViewModel` is gone too, an unguarded `setIntent()`
would republish the shared text into a fresh `_sharedTextState` and import the same URL a second time.
A plain rotation is already safe without the guard, since the `MainViewModel` survives it with
`_sharedTextState` already cleared. The guard does **not** change deep-link behaviour in either
direction — after a rotation the surviving ViewModel still holds the Intent, so
`LaunchedEffect(intent)` re-runs `handleDeepLink()` on the retained value regardless. Do not try to
suppress that here; it is out of scope.

**`setContent`:** collect `viewModel.sharedTextState` alongside the existing `collectAsState()` calls
and pass it, together with `viewModel::onSharedTextHandled`, into `NextcloudCookbookApp`. The
signature becomes:

```kotlin
fun NextcloudCookbookApp(
    intent: Intent?,
    sharedText: String?,
    onSharedTextHandled: () -> Unit,
)
```

The call site is already inside the `CompositionLocalProvider` that provides `LocalCredentials`, so
that local is readable from within the composable.

**Inside `NextcloudCookbookApp`,** next to the existing deep-link `LaunchedEffect`:

- obtain a `DestinationsNavigator` via `navController.rememberDestinationsNavigator()` and
  `val currentDestination by navController.currentDestinationAsState()`, both from
  `com.ramcosta.composedestinations.utils`. `BottomBar.kt` already uses
  `rememberDestinationsNavigator()` — follow it.

  Caution on naming: the `composable(SplashScreenDestination) { … }` block further down already refers
  to `destinationsNavigator`, which there resolves to the `DestinationScope` receiver's own property.
  Naming the new outer local `destinationsNavigator` too creates a shadowing question that is easy to
  get wrong. Prefer a distinct name for the outer one, and if you do reuse the name, verify the splash
  screen still navigates.

- add a `LaunchedEffect` keyed on `sharedText`, `currentDestination` and `LocalCredentials.current`
  that returns early unless **all** of these hold:
  - `sharedText != null`
  - `currentDestination != null`
  - `currentDestination != SplashScreenDestination`
  - credentials are non-null

  then navigates to `DownloadRecipeScreenDestination(sharedText = sharedText)` with
  `popUpTo(DownloadRecipeScreenDestination) { inclusive = true }`, and finally invokes
  `onSharedTextHandled()`.

**The `currentDestination != null` check is load-bearing, not defensive.**
`currentDestinationAsState()` is `currentDestinationFlow.collectAsState(initial = null)`, so it emits
`null` on the first composition of a cold start and only reports a real destination once the first
back-stack entry arrives. A gate written as "anything other than `SplashScreenDestination`" passes
during that first frame and navigates underneath the splash — and `SplashScreen` then navigates to
`HomeScreen` with `popUpTo(SplashScreenDestination) { inclusive = true }`, which pops the import screen
that was pushed before it. That is precisely the race the gate exists to prevent.

**Clear the state only after a successful navigation.** That ordering is what makes requirement 6 —
"share while logged out" — work with no extra code: the shared text stays pending, and
`MainViewModel.authState` re-emits when `accountRepository.getAccount()` produces an account after
login, which re-fires the effect. Do not add a separate logged-out branch.

`popUpTo(DownloadRecipeScreenDestination) { inclusive = true }` is what keeps two consecutive shares
from stacking two import screens (requirement 7).

## Acceptance criteria

- [ ] The app appears in the system share sheet for `text/plain`, with its own name and icon.
- [ ] A shared URL opens `DownloadRecipeScreen` with the download already running.
- [ ] A shared non-URL opens the same screen with the raw text prefilled and no download started.
- [ ] Sharing while the app is running does not restart it at the splash screen and does not stack import screens.
- [ ] Sharing while logged out holds the URL and imports after the login flow completes.
- [ ] Styled text (a `Spanned` payload) is not silently dropped.
- [ ] No new string resource, no `ACTION_SEND_MULTIPLE`, no `EXTRA_STREAM`, no `ACTION_VIEW` filter.

## Verification

```
./gradlew ktlintCheck test lint
```

`MainViewModel`'s intent branch and the navigation gating both need a real `Intent` and a live nav
host, so they are verified by hand rather than by unit test — adding Robolectric for two branches is
not worth it (§4.3). Install the debug build and send it a share intent:

```
adb shell am start -a android.intent.action.SEND -t text/plain \
  --es android.intent.extra.TEXT "https://example.com/recipe" \
  -n de.lukasneugebauer.nextcloudcookbook.debug/de.lukasneugebauer.nextcloudcookbook.core.presentation.MainActivity
```

Walk all ten cases of §4.3. The ones most likely to fail on a first attempt, and which must not be
skipped:

- **Case 1, cold start, logged in** — splash, then the import screen with the download running, then
  the recipe detail. Back goes to home, *not* back to the import screen. This is the case the null gate
  and the task 2 back-stack fix both protect.
- **Case 3, logged out** — the login flow appears first; after signing in, the import screen opens with
  the shared URL and imports.
- **Case 5, process death** — share a URL, background the app, kill it with
  `adb shell am kill de.lukasneugebauer.nextcloudcookbook.debug`, then reopen from recents. The import
  screen is restored *without* a second import. This is the case the `savedInstanceState` guard and
  task 2's `autoImportTriggered` flag exist for; rotation exercises neither.
- **Case 10, real share sheet** — share a page from an actual browser, not just via `adb`. The `adb`
  command bypasses the share sheet entirely, so it cannot confirm the manifest filter is discoverable.
