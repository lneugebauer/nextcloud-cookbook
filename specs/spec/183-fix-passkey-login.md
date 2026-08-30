# Fix: sign in with a passkey (WebAuthn)

Issue: [#183 — Unable to login using Authentik Passkey (WebAuthn)](https://github.com/lneugebauer/nextcloud-cookbook/issues/183)

## 1. Goals & Requirements

### Problem

Choosing "Login with Passkey" on the sign-in page fails immediately with "Authentication failed".
The Android passkey/biometric sheet never appears. Reported against a Nextcloud instance backed by
an Authentik SSO provider, but the failure is not Authentik-specific — it applies to every WebAuthn
login, including Nextcloud's own passkey support and hardware security keys.

### Root cause

`WebViewLoginScreen.kt:98`–`:105` loads the Nextcloud Login Flow v2 login URL into a bare
`android.webkit.WebView` with only `settings.javaScriptEnabled = true` (`:99`). A WebView has WebAuthn
disabled by default: `WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_NONE` is "the support level that
disables WebAuthn requests from WebView. This is the default behavior."
([`WebSettingsCompat.java`, androidx-main](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/webkit/webkit/src/main/java/androidx/webkit/WebSettingsCompat.java))

So `navigator.credentials.get()` rejects inside the page, and the identity provider renders its
generic "Authentication failed" — exactly the screenshot in the issue.

The issue thread points at
[Authenticate users with WebView](https://developer.android.com/identity/sign-in/credential-manager-webview)
as the likely fix. It is not — see §2.1. The fix is to stop using a WebView for sign-in.

### Requirements

1. A user whose Nextcloud (or its upstream IdP) requires a passkey can complete sign-in.
2. The same holds for anything else the WebView blocks or degrades: hardware security keys, browser
   password managers, client certificates, existing IdP sessions.
3. Password and 2FA sign-in keep working unchanged.
4. After a successful sign-in the user lands in the app, signed in, without manual steps.
5. Works in the F-Droid `full` flavour — no Google Play Services dependency.

### Out of scope

- Changes to `ManualLoginScreen` (app-password sign-in). It stays as-is and becomes the documented
  fallback for the self-signed case (§2.3).
- The unused `allowSelfSignedCertificates` navigation argument on `ManualLoginScreenDestination`.
  It is removed from the sign-in destination this spec rewrites, but touching `ManualLoginScreen`
  is a separate cleanup.
- Adding `ActivityNotFoundException` handling to the pre-existing `Uri.openInBrowser`
  (`UriOpenInBrowserExtension.kt:7`). The new launcher handles it; retro-fitting the old one is a
  separate change.

## 2. Architecture & Design Decisions

### 2.1 Rejected: Credential Manager inside the WebView

**Decision.** Do not add `androidx.credentials` + `WebSettingsCompat.setWebAuthenticationSupport()`.
It cannot work for this app.

**Grounding:** research, from the AOSP javadoc for the two non-default support levels
([`WebSettingsCompat.java`](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/webkit/webkit/src/main/java/androidx/webkit/WebSettingsCompat.java)):

- `WEB_AUTHENTICATION_SUPPORT_FOR_APP` — "allows WebAuthn requests **for the app in which the
  WebView is embedded**. See Digital Asset Links to learn how to associate an app with a website."
  The request is routed through the native app path, so the assertion carries an
  `android:apk-key-hash:…` origin and Play Services will only mint it if the relying party's domain
  serves `/.well-known/assetlinks.json` delegating `delegate_permission/common.get_login_creds` to
  this app's package and signing certificate. This app talks to **arbitrary self-hosted domains**;
  every user would have to edit their own server's asset links, and Authentik users their IdP's as
  well. Not deliverable.
- `WEB_AUTHENTICATION_SUPPORT_FOR_BROWSER` — "allows apps to make WebAuthn calls for **any**
  website. See **Privileged apps**." That is Play Services' FIDO2 privileged-browser path, reserved
  for registered browsers; the app-facing API is the one that requires Digital Asset Links
  ([fido-dev list](https://groups.google.com/a/fidoalliance.org/g/fido-dev/c/jjyWEAv4PR4/m/D9oimTNlAgAJ)).
  A recipe client is not going to be allowlisted.

Two further blockers, either of which is independently disqualifying:

- The documented setup pulls `androidx.credentials:credentials-play-services-auth`, a Google Play
  Services binding. The `full` flavour ships on
  [F-Droid](https://f-droid.org/packages/de.lukasneugebauer.nextcloudcookbook/) (`README.md:4`).
- Even with asset links in place, the passkey the user enrolled in a browser is bound to the
  `https://cloud.example.tld` origin. An `apk-key-hash` assertion is a different origin and the
  server would have to be configured to accept it.

### 2.2 Replace the WebView with a Chrome Custom Tab

**Decision.** Add `androidx.browser:browser:1.10.0`. Open the Login Flow v2 login URL in a Custom
Tab, keep polling the poll endpoint in the ViewModel exactly as today, and delete the WebView
sign-in screen and `WebViewClient`.

**Grounding:** research, and it is what the server protocol already asks for. Nextcloud's own
developer manual says the v2 login URL "should be opened in the **default browser**, this is where
the user will follow the login procedure", and that the client "should directly start polling the
poll endpoint until authentication is done"
([Login Flow, Nextcloud stable developer manual](https://docs.nextcloud.com/server/stable/developer_manual/client_apis/LoginFlow/index.html)).
The same page gives the reason v2 exists: v1's WebView had "special hurdles" — proxies, client
certificates — that the user's own browser does not.

**Why this fixes #183.** In a real browser, `navigator.credentials.get()` is handled by the browser's
own WebAuthn implementation against the `https://` origin the passkey was enrolled for. No asset
links, no privileged API, no per-domain setup. Password managers, security keys, and any existing
IdP session in the browser come along for free.

**Why no redirect handling is needed.** Login Flow v2 hands credentials back through the poll
endpoint, not a redirect. The token is "valid for 20 minutes" and the successful `200` "will only be
returned once" (same page). The existing `pollLoginServer` recursion
(`WebViewLoginViewModel.kt:124`–`141`) already implements this and is carried over unchanged.

**Version.** `androidx.browser:browser:1.10.0` is the current stable release (25 March 2026) and
requires `minSdk 23` ([Browser release notes](https://developer.android.com/jetpack/androidx/releases/browser));
the app is on `minSdk 25` (`app/build.gradle:39`). No Play Services, so the F-Droid flavour is fine.

**Fallback.** If no browser is installed at all, `launchUrl` throws `ActivityNotFoundException`.
Catch it and surface an error state rather than crashing.

### 2.3 Self-signed certificates: the option stays, the silent bypass does not

**Decision.** Keep the "Allow self-signed certificates" option exactly as it is. Always use the
Custom Tab. What changes is only that the *browser's* certificate warning is no longer bypassed for
the user automatically.

**The option is not removed, and self-signed password sign-in is still a supported path.** The
checkbox drives `OkHttpClientProvider` (`OkHttpClientProvider.kt:32`–`50`) through
`PreferencesManager`, which is what the app's own calls use — `POST /index.php/login/v2`, every poll
request, and every subsequent API call. That is unchanged and is what makes the app usable against a
self-signed server at all. The intended flow stays:

1. Tick "Allow self-signed certificates" and tap **Sign in**.
2. The app's `POST /index.php/login/v2` succeeds over the trust-all client and the Custom Tab opens.
3. The browser shows its certificate warning; the user taps *Advanced → Proceed*.
4. Password (and 2FA) sign-in completes; the app polls with the trust-all client and signs in.
5. All subsequent API traffic keeps using the trust-all client.

**What is actually lost.** `WebViewClient.onReceivedSslError` → `handler.proceed()`
(`WebViewClient.kt:22`–`30`) silently accepted the bad certificate for the login page. Step 3 above
now requires a manual tap through a deliberately alarming interstitial. That is the whole regression.

**Verified on a device — the override is offered.** Checked 2026-08-30 against
`https://self-signed.badssl.com/` (`NET::ERR_CERT_AUTHORITY_INVALID`, the same error class a
self-signed Nextcloud produces), opened in a Custom Tab from Slack: the interstitial rendered, the
certificate could be trusted, and the page loaded — inside the tab, which kept its Custom Tabs
chrome throughout (default-browser icon top right, close ✕ top left). No documentation settles this
either way — the Chromium
[Custom Tabs security FAQ](https://chromium.googlesource.com/chromium/src/+/refs/heads/main/docs/security/custom-tabs-faq.md)
covers the trust model but not interstitials — so the device check is the evidence. Step 3 stands,
and no WebView fallback is needed for self-signed users.

**Reproducing the check.** `https://self-signed.badssl.com/` needs no server setup. Get it in front
of a Custom Tab by tapping the URL inside any app that opens links in one — identifiable by the slim
origin toolbar with a ✕ and an "Open in Chrome" overflow item. Worth repeating with Firefox or
Samsung Internet as default if a report suggests a provider-specific difference, since the Custom
Tabs provider is whichever browser the user has chosen.

**Installing the certificate remains the better answer, and the FAQ says so.** Chrome's certificate
verifier "considers local trust decisions for both adding and removing trust" for TCP-based TLS
([Chrome Root Store FAQ](https://chromium.googlesource.com/chromium/src/+/main/net/data/ssl/chrome_root_store/faq.md)),
and `network_security_config.xml` already trusts user certificates — so a certificate installed into
the Android user trust store removes the warning entirely for both the browser and the app, and
makes passkeys work. That is advice in `docs/faqs.md` (§3.9), not a code path.

**Passkeys and self-signed certificates are mutually exclusive regardless of this decision.** Since
M110, Chrome refuses WebAuthn requests on sites with TLS certificate errors — "using the same
criteria for showing danger interstitials or a 'Not secure' pill on the omnibox" — with the
`AllowWebAuthnWithBrokenTlsCerts` enterprise policy defaulting to *block*
([Chrome Enterprise policy list](https://chromeenterprise.google/policies/#AllowWebAuthnWithBrokenTlsCerts)).
Clicking through the interstitial does not re-enable it. So a self-signed server cannot offer
passkeys in any browser-based flow, and this section only ever concerns password/2FA users.
Installing the certificate properly is the only route that gets both.

**Mitigation as specified.** A FAQ entry (§3.9). No in-app copy change — the checkbox label still
describes exactly what it does for the app's own connections.

### 2.4 Returning to the app: best-effort bring-to-front

**Decision.** When the poll succeeds, navigate to Home, then start `MainActivity` with
`FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP`.

**Why this shape.** `CustomTabsIntent.launchUrl` is called with the Activity context, so the browser
activity joins the app's task and sits directly on top of `MainActivity`. `CLEAR_TOP` finishes
everything above `MainActivity` — which is the Custom Tab — and `SINGLE_TOP` makes the system
deliver `onNewIntent` to the existing instance instead of destroying and recreating it, preserving
the Compose navigation back stack. `MainActivity` already implements `onNewIntent`
(`MainActivity.kt:113`). There is no public API to close a Custom Tab; this is the standard pattern.

**Verified safe against the deep-link path.** `MainActivity.handleIntent` forwards every intent to
`MainViewModel`, and `NextcloudCookbookApp` only calls `navController.handleDeepLink(intent)` when
`intent?.data != null && intent.action != ACTION_MAIN` (`MainActivity.kt:184`–`188`). An
`Intent(context, MainActivity::class.java)` has neither data nor action, so it is ignored there.

**needs-review — this is best-effort, not guaranteed.** While the Custom Tab is on top, the app has
no visible window, so the call is a background activity start. The current
[Restrictions on starting activities from the background](https://developer.android.com/guide/components/activities/background-starts)
page lists eight exemptions and **none of them is "the app has an activity in the back stack of the
foreground task"** — that exemption appears in older revisions of the same page and in secondary
sources, but not in the list Google publishes today, and Android 15/16 tightened these rules
further. A blocked start fails silently with `Background activity launch blocked!` in logcat under
the `ActivityTaskManager` tag.

**Therefore the design must degrade gracefully, and does.** The navigation to Home happens
independently of the activity start. If the start is blocked, the user presses Back once and is
already signed in and on the Home screen — no error, no repeated sign-in. Verify on real devices
across the supported range (§4.3) and, if it turns out to be blocked on current Android, drop the
`startActivity` call rather than reaching for `SYSTEM_ALERT_WINDOW` or a notification `PendingIntent`.

### 2.5 The success signal must not depend on recomposition

**Decision.** In the new screen, collect the ViewModel state inside a `LaunchedEffect(Unit)` that
suspends on the flow — **not** the existing `LaunchedEffect(uiState)` pattern
(`WebViewLoginScreen.kt:48`).

**Grounding:** research, read from the resolved 1.10.0 sources in the Gradle cache. This is the one
way §2.4 could silently do nothing.

While the Custom Tab is in front, `MainActivity` is stopped. On `ON_STOP`, `WindowRecomposer`
calls `recomposer.pauseCompositionFrameClock()` — "Pause the recomposer's frame clock which will
pause all calls to `withFrameNanos` (e.g. animations) while the window is stopped"
(`WindowRecomposer.android.kt:410`–`414`, `androidx.compose.ui:ui-android:1.10.0`). The recompose
loop awaits exactly that clock: `parentFrameClock.withFrameNanos { … }` inside
`runRecomposeAndApplyChanges` (`Recomposer.kt:609`). A `LaunchedEffect` keyed on state re-fires only
after a recomposition commits, so it may not run until the user returns — the moment at which
bringing the app to the front is pointless.

A coroutine already suspended inside a `LaunchedEffect` is not subject to this.
`AndroidUiDispatcher.dispatch` posts the resumption to the main-thread `Handler` *and* schedules a
Choreographer callback, and whichever fires first drains the queue
(`AndroidUiDispatcher.android.kt:133`–`144`). The handler message is delivered regardless of frames,
so the collector resumes while the activity is stopped.

The same coroutine is also what launches the Custom Tab, so the whole sign-in handshake lives in one
place:

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
            BrowserLoginScreenState.Authenticated -> {
                navigator.navigate(HomeScreenDestination) {
                    popUpTo(StartScreenDestination) { inclusive = true }
                }
                context.getActivity()?.startActivity(
                    Intent(context, MainActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP,
                    ),
                )
            }
            else -> Unit
        }
    }
}
```

Navigate **before** the activity start: `NavController` state is updated directly and does not need
a frame, so by the time the app comes forward it is already rendering Home.

### 2.6 Survive process death: persist the poll token

**Decision.** Write `pollUrl` and `token` into the `SavedStateHandle` as soon as
`getLoginEndpoint` succeeds. On construction, if both are present, skip `getLoginEndpoint` and
resume polling the existing token.

**Why this is new work and not scope creep.** With the in-app WebView, the app was foreground for
the whole sign-in. With a Custom Tab it is backgrounded for as long as an SSO round-trip takes —
minutes, with a passkey prompt and possibly 2FA — which is exactly when Android reclaims processes.
Today, a restart re-runs `init` → `getLoginEndpoint` (`WebViewLoginViewModel.kt:43`–`46`) and mints
a **fresh** token, orphaning the login the user just completed against the old one. They would
authenticate successfully and still land back on the sign-in screen.

**Grounding:** convention. `SavedStateHandle` is already injected into this ViewModel
(`WebViewLoginViewModel.kt:36`) and into `ManualLoginViewModel` (`ManualLoginViewModel.kt:32`); this
uses the same handle for writes as well as reads.

**Persist `browserLaunched` alongside them.** Without it the restored state defaults to
`browserLaunched = false` and §2.5's collector re-launches the Custom Tab the instant the screen
comes back — a surprise browser launch on top of a tab the user may already have open, and a visible
flash in the common case where they *have* finished signing in and the next poll is about to
succeed. Restoring it as `true` instead lands the user on the waiting screen with the **Open
browser** button available, which is predictable in both directions. Four keys in total: `pollUrl`,
`pollToken`, the login URL, and `browserLaunched`.

**Bound.** The token is valid for 20 minutes server-side; an expired one simply keeps returning
`404` and the user can use the retry action. No extra expiry bookkeeping in the client.

### 2.7 No `<queries>` element is required

**Decision.** `AndroidManifest.xml` is unchanged.

**Grounding:** research, plus existing evidence in the app. Android 11 package visibility filters
`queryIntentActivities()` / `resolveActivity()`, not implicit `startActivity` — launching a Custom
Tab via `CustomTabsIntent.Builder().build().launchUrl()` needs no manifest declaration; `<queries>`
with `android.support.customtabs.action.CustomTabsService` is only needed to *detect* Custom
Tabs support or to bind the service for warm-up
([Using Custom Tabs with Android 11](https://developer.chrome.com/blog/custom-tabs-android-11)).
The app already relies on this: `Uri.openInBrowser` (`UriOpenInBrowserExtension.kt:7`) fires a bare
implicit `ACTION_VIEW` and works from the Settings and Info screens with no `<queries>` present.

Consequence for the implementation: do **not** null-check with `Intent.resolveActivity` — it returns
`null` without `<queries>` and would report "no browser" on every Android 11+ device. Catch
`ActivityNotFoundException` instead.

### 2.8 New test dependency

**Decision.** Add `testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2'`.

**Grounding:** required. `BrowserLoginViewModel.init` launches into `viewModelScope`
(`Dispatchers.Main.immediate`), unavailable on the JVM without `Dispatchers.setMain`, and the poll
retry path needs virtual time to test the 5 s `delay` without a real wait. Version matches the
coroutines version already resolved for the project.

> Note: [`specs/spec/208-…`](./208-fix-copy-single-ingredient-copies-default-amount.md) §2.5 adds the
> same dependency. Whichever lands first adds it; the second just uses it.

CI runs `bundle exec fastlane build` → `clean, ktlint, lint, test, assemble`
(`.github/workflows/ci.yml:32`, `fastlane/Fastfile:112`–`118`), so these unit tests run on every PR.
`connectedAndroidTest` does not run in CI, which is why the passkey fix itself is covered by manual
verification (§4.3).

## 3. Implementation Changes

### 3.1 `app/build.gradle`

```groovy
implementation 'androidx.browser:browser:1.10.0'
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2'
```

Put the first next to the other `androidx.*` entries (~line 126) and the second next to the existing
`testImplementation` entries (~line 177).

### 3.2 New — `core/util/UriOpenInCustomTabExtension.kt`

Sits beside the existing `UriOpenInBrowserExtension.kt` and follows its `Uri` receiver shape.
Returns `false` when nothing can handle the intent so the caller can show an error (§2.7: no
`resolveActivity` pre-check).

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

Pass the **Activity** context (§2.4) — the caller uses `LocalContext.current`, which is the
Activity's context wrapper inside `setContent`. An application context would force
`FLAG_ACTIVITY_NEW_TASK`, put the tab in its own task, and break `CLEAR_TOP`.

### 3.3 `auth/domain/state/WebViewScreenState.kt` → `BrowserLoginScreenState.kt`

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

`pollLoginServerIsActive` is dropped: `Loaded` is now the only polling state, so the guard at
`WebViewLoginViewModel.kt:137` becomes `_uiState.value is Loaded`. `browserLaunched` replaces it as
a one-shot guard so the tab is opened exactly once per `Loaded` state.

### 3.4 New — `auth/presentation/browser/BrowserLoginViewModel.kt`

A rename-and-trim of `WebViewLoginViewModel`. Same constructor injections, same
`observeAuthorizationStatus()` (`:93`–`121`) and `pollLoginServer()` (`:124`–`141`) bodies, same
`POLL_DELAY = 5_000L`.

| Change | Detail |
| --- | --- |
| Remove | `onWebViewLoadError` (`:52`–`62`) — no WebView left to report load errors |
| Add | `fun onBrowserLaunched()` — sets `Loaded.browserLaunched = true` |
| Add | `fun onNoBrowserAvailable()` — `Error(UiText.StringResource(R.string.error_no_browser))` |
| Add | `fun onOpenBrowserClick()` — sets `browserLaunched = false` so §2.5's collector re-launches the tab |
| Change | `init` reads `savedStateHandle["pollUrl"]` / `["pollToken"]` first; if both non-null, set `Loaded(loginUrl, browserLaunched = savedStateHandle["browserLaunched"] ?: false)` from the saved login URL and resume `pollLoginServer` without calling `getLoginEndpoint` (§2.6) |
| Change | `getLoginEndpoint` writes `pollUrl`, `pollToken`, and the login URL string back into `savedStateHandle` on success, before entering `pollLoginServer` |
| Change | `onBrowserLaunched()` / `onOpenBrowserClick()` also write `browserLaunched` into `savedStateHandle`, so the flag survives process death (§2.6) |
| Change | `retry()` (`:64`–`68`) clears all four saved keys before re-requesting |
| Change | Polling guard `_uiState.value is BrowserLoginScreenState.Loaded` |

`Uri` is not `Parcelable`-free in a `SavedStateHandle` — store the login URL as a `String` and
`toUri()` it on read, matching how `LoginEndpointResponse.toLoginEndpointResult` already converts
(`LoginEndpointResponse.kt:20`).

### 3.5 New — `auth/presentation/browser/BrowserLoginScreen.kt`

Replaces `WebViewLoginScreen.kt`. Keeps the `@Destination<MainGraph>` annotation, `HideBottomNavigation()`,
the `Scaffold` + back-arrow `TopAppBar`, `Loader`, and `AbstractErrorScreen` with `onRetryClick`.

Signature: `url: String` only. Drop `allowSelfSignedCertificates` — it existed solely to configure
`WebViewClient`, and the preference is already persisted independently of navigation —
`StartScreenViewModel` writes it in `init` (`StartScreenViewModel.kt:29`) and on every checkbox
toggle (`:42`–`:43`) — and consumed by
`OkHttpClientProvider`.

State rendering:

| State | UI |
| --- | --- |
| `Initial` | `Loader` |
| `Loaded` | New private `BrowserLoginLayout`: centred `Text(R.string.login_browser_waiting)`, a `Loader`, and a `DefaultTextButton(R.string.login_browser_open)` wired to `viewModel::onOpenBrowserClick` for when the user dismissed the tab |
| `Authenticated` | `Loader` (the §2.5 collector is navigating) |
| `Error` | `AbstractErrorScreen(onRetryClick = viewModel::retry)` |

Side effects: the single `LaunchedEffect(Unit)` collector from §2.5. Nothing else observes `uiState`
for effects.

Add a `@Preview` for `BrowserLoginLayout`, matching `StartLayoutPreview` (`StartScreen.kt:236`).

### 3.6 Deletions

- `auth/presentation/webview/WebViewLoginScreen.kt`
- `auth/presentation/webview/WebViewLoginViewModel.kt`
- `auth/domain/WebViewClient.kt` — `WebViewLoginScreen.kt:101` is its only usage

### 3.7 `auth/domain/state/StartScreenState.kt` and `auth/presentation/start/StartScreen.kt`

| Location | Change |
| --- | --- |
| `StartScreenState.kt:19` | `object WebView` → `object SignIn` — the event expresses the user's intent, not the transport |
| `StartScreen.kt:42` | import `BrowserLoginScreenDestination` instead of `WebViewLoginScreenDestination` |
| `StartScreen.kt:71`–`79` | `StartScreenSignInEvent.SignIn -> navigator.navigate(BrowserLoginScreenDestination(url = data.url))` — no `allowSelfSignedCertificates` argument |
| `StartScreen.kt:111` | `onSignInClick = { viewModel.onLoginClick(event = StartScreenSignInEvent.SignIn) }` |
| `StartScreen.kt:131`, `:179`, `:197`, `:242` | rename the `onWebViewLoginClick` parameter to `onSignInClick` |

`StartScreenViewModel` is unchanged.

### 3.8 `app/src/main/res/values/strings.xml`

Add (alphabetical, matching the file's ordering):

```xml
<string name="error_no_browser">No browser app found. Install a browser or use manual sign in.</string>
<string name="login_browser_open">Open browser</string>
<string name="login_browser_waiting">Complete the sign in in your browser. This screen updates automatically.</string>
```

Remove `error_webview_load_failed` (`:66`) — its only caller was `onWebViewLoadError`. Translations
in `values-*/strings.xml` are managed by Weblate; delete only the `values/` entry and let Weblate
reconcile.

### 3.9 `docs/faqs.md`

Add an entry after "Can I connect via http?", following the file's badge convention:

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

### 3.10 Housekeeping

- Run `./gradlew ktlintFormat`; `ktlint` runs before `test` in the `build` lane
  (`fastlane/Fastfile:112`–`118`).
- Commit with a conventional-commit `fix:` subject describing the user-visible behaviour. Release
  notes derive from commit messages; there is no `CHANGELOG.md`. Reference #183 in the pull request,
  not the commit body.
- Do not touch `fastlane/metadata/android/*/changelogs/`.
- `aboutlibraries` picks up the new dependency automatically at build time; no manual licence entry.

## 4. Test Cases

### 4.1 New — `app/src/test/java/…/auth/presentation/browser/BrowserLoginViewModelUnitTest.kt`

There is no existing ViewModel test to extend. Follow `SyncRecipesUseCaseUnitTest` — plain `mock()`
fields assigned in `@Before`, rather than `@Mock` + `MockitoAnnotations.openMocks`.

#### Fixture

| Collaborator | Setup |
| --- | --- |
| `authRepository` | `mock()`; `getLoginEndpoint(any())` → `Resource.Success(LOGIN_ENDPOINT)` |
| `accountRepository` | `mock()`; `getAccount()` → `flowOf(Resource.Error(…))` by default so `observeAuthorizationStatus` stays inert unless a test drives it |
| `ncCookbookApiProvider` | `mock()`; `apiFlow` → `MutableStateFlow(null)` |
| `preferencesManager` | `mock()` |
| `clearPreferencesUseCase` | `mock()` |
| `savedStateHandle` | real `SavedStateHandle(mapOf("url" to "https://cloud.example.tld"))` — the `@VisibleForTesting` map constructor, no `Bundle` involved (`testOptions.unitTests.returnDefaultValues` is not enabled) |

```kotlin
// Uri is an Android framework type; see the note below for why this is a mock, not a real Uri.
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

`LOGIN_RESULT` is what `tryLogin` returns on success — `LoginResult.ncAccount` is non-null and is
dereferenced by `pollLoginServer` (`WebViewLoginViewModel.kt:130`), so cases 3 and 8 need it. The
`NcAccount` shape is copied from the existing fixture at `RecipeRepositoryImplUnitTest.kt:772`.

`@Before` sets `Dispatchers.setMain(StandardTestDispatcher())`; `@After` calls `Dispatchers.resetMain()`.
Use `StandardTestDispatcher` (not `Unconfined` as in spec 208) — the retry cases need
`advanceTimeBy` over the 5 s `POLL_DELAY`. `runTest` picks up the scheduler of the `TestDispatcher`
installed via `Dispatchers.setMain`, so virtual time is shared without passing a scheduler around;
construct the ViewModel inside each `runTest` body so `init` runs under that scheduler.

`Uri.toUri()`/`Uri.parse` is an Android framework call. Either add
`testOptions { unitTests.returnDefaultValues = true }` — which changes behaviour for every existing
unit test and is **not** recommended — or keep `Uri` out of assertions by comparing
`state.loginUrl.toString()` and constructing fixtures with Robolectric-free doubles. **Simplest
option, and the one to take:** make the fixture `Uri` a `mock<Uri>()` whose `toString()` is stubbed,
since the ViewModel only stores and forwards it. Flag at review if a cleaner shape is preferred.

#### Cases

**1. `init_withValidUrl_requestsLoginEndpointAndEntersLoaded`**
- Assert: `verify(authRepository).getLoginEndpoint("https://cloud.example.tld")`;
  state is `Loaded` with `browserLaunched == false`.

**2. `init_withoutUrl_entersErrorState`**
- Fixture: `SavedStateHandle(emptyMap())`
- Assert: state is `Error` with `R.string.error_invalid_url`; `authRepository` never touched.

**3. `init_withSavedPollToken_resumesPollingWithoutNewEndpointRequest`** — guard for §2.6.
- Fixture: `SavedStateHandle` pre-seeded with `url`, `pollUrl`, `pollToken`, the login URL, and
  `browserLaunched = true`
- Assert: `verify(authRepository, never()).getLoginEndpoint(any())`;
  `verify(authRepository).tryLogin(SAVED_POLL_URL, SAVED_TOKEN)`; state is `Loaded` with
  `browserLaunched == true`, i.e. the collector will **not** re-open the tab (§2.6).

**4. `getLoginEndpoint_onSuccess_persistsPollTokenToSavedStateHandle`** — the other half of §2.6.
- Assert: `savedStateHandle.get<String>("pollToken") == "token-1"` and `"pollUrl"` matches.

**5. `onBrowserLaunched_setsBrowserLaunchedFlag`**
- Assert: `(state as Loaded).browserLaunched` is `true`; guards against reopening the tab on every
  emission.

**6. `onOpenBrowserClick_clearsBrowserLaunchedFlag`**
- Act: `onBrowserLaunched()` then `onOpenBrowserClick()`
- Assert: `browserLaunched` is `false` — this is what makes "Open browser" re-launch the tab.

**7. `onNoBrowserAvailable_entersErrorState`**
- Assert: `Error` with `R.string.error_no_browser`.

**8. `pollLoginServer_onError_retriesAfterPollDelay`**
- Fixture: `tryLogin` returns `Resource.Error` then `Resource.Success`
- Act: `advanceTimeBy(POLL_DELAY + 1)`
- Assert: `verify(authRepository, times(2)).tryLogin(any(), any())`;
  `verify(preferencesManager).updateNextcloudAccount(any())`; `verify(ncCookbookApiProvider).initApi()`.

**9. `pollLoginServer_stopsWhenStateLeavesLoaded`**
- Act: drive the state to `Error` (via `onNoBrowserAvailable()`), then `advanceTimeBy(POLL_DELAY * 3)`
- Assert: `tryLogin` invoked exactly once — the `is Loaded` guard replacing `pollLoginServerIsActive`
  still stops the recursion.

**10. `retry_clearsSavedPollTokenAndRequestsNewEndpoint`**
- Assert: `savedStateHandle.get<String>("pollToken") == null` immediately after `retry()`, then a
  second `getLoginEndpoint` call. Without this, retry would resume a dead token forever.

**11. `onBrowserLaunched_persistsFlagToSavedStateHandle`** — the other half of the §2.6 restore path.
- Act: `onBrowserLaunched()`
- Assert: `savedStateHandle.get<Boolean>("browserLaunched") == true`; then `onOpenBrowserClick()`
  flips it back to `false`. Case 3 covers the read side; this covers the write side.

### 4.2 `app/src/androidTest/…/screenshots/ScreenshotsTestSuite.kt`

Required: rename `onWebViewLoginClick = {}` → `onSignInClick = {}` at `:68`, otherwise the
`androidTest` source set stops compiling.

Optional: add a `browserLoginScreen()` case rendering `BrowserLoginLayout`, mirroring
`manualLoginScreen()` at `:76`. Not required for the fix; skip unless store screenshots are being
refreshed anyway.

### 4.3 Manual verification (not automated — `connectedAndroidTest` does not run in CI)

The actual bug is only reproducible against a real IdP, so these are the acceptance steps:

1. **The reported case.** Nextcloud fronted by Authentik with a passkey enrolled. Enter the server
   URL → **Sign in**. A Custom Tab opens; choose "Login with Passkey"; the Android passkey sheet
   appears; authenticate. The app returns to Home, signed in. *(This is #183.)*
2. **Password + 2FA.** Same flow on a plain Nextcloud — unchanged behaviour.
3. **Security key.** Same flow with a USB/NFC FIDO2 key.
4. **Self-signed certificate (§2.3).** Server with an untrusted certificate, "Allow self-signed
   certificates" ticked. Confirm the Custom Tab's interstitial offers **Advanced → Proceed**, that
   proceeding loads the login page, that password sign-in completes, and that the app then works
   (recipe list loads over the trust-all client). Confirm **Manual sign in** with an app password
   still works as the documented alternative. The interstitial-override half of this was already
   verified standalone against `https://self-signed.badssl.com/` (§2.3); what remains here is the
   end-to-end, which needs a real server.
5. **Return-to-app (§2.4), on the widest device range available — at minimum one Android 13-or-older
   and one Android 15-or-newer device.** After a successful sign-in, confirm the tab closes and the
   app comes forward on its own. If it does not, check logcat for
   `ActivityTaskManager: Background activity launch blocked!` and confirm the fallback: pressing Back
   once lands on Home, signed in.
6. **Tab dismissed mid-flow.** Swipe the Custom Tab away before authenticating → the app shows
   "Complete the sign in in your browser"; **Open browser** re-opens the same login URL and finishing
   there still signs in (same token).
7. **Process death (§2.6).** With the tab open, enable *Don't keep activities* in developer options
   (or `adb shell am kill de.lukasneugebauer.nextcloudcookbook.debug`), complete the browser sign-in,
   then return. The app resumes polling the same token and signs in rather than restarting the flow.
8. **Back out.** Press Back from the Custom Tab without signing in → the sign-in screen, then Back
   again → the start screen. No crash, no orphaned polling.

## 5. Readiness

**Ready to implement.** No `needs-research` markers remain and no decision is unresolved. One
best-effort behaviour (§2.4) still has to be confirmed on a device *during* implementation, but it
does not block starting and its remedy is decided in advance.

Verified rather than assumed:

- WebAuthn is off by default in `WebView`, and both non-default support levels are unreachable for
  this app — read from the AOSP `WebSettingsCompat` javadoc, corroborated by the fido-dev list on
  the privileged vs. app-facing API split (§2.1). This is why the fix is "leave the WebView", not
  "configure the WebView".
- Nextcloud's own protocol documentation prescribes the default browser for Login Flow v2 and
  token-based polling with no redirect, so no deep-link plumbing is needed (§2.2).
- `androidx.browser:browser:1.10.0` is current and needs `minSdk 23` against the app's 25; it pulls
  no Play Services, so the F-Droid `full` flavour is safe (§2.2).
- Implicit `ACTION_VIEW` launches need no `<queries>` — confirmed by the Chrome team's Android 11
  Custom Tabs post *and* by `Uri.openInBrowser` already working in the shipped app (§2.7).
- Recomposition is frame-clock gated while the activity is stopped
  (`WindowRecomposer.android.kt:410`, `Recomposer.kt:609`), while `AndroidUiDispatcher.dispatch`
  posts to the `Handler` independently of frames (`AndroidUiDispatcher.android.kt:133`–`144`) — both
  read from the resolved 1.10.0 sources. This is what forces the `LaunchedEffect(Unit)` collector in
  §2.5 instead of the codebase's usual state-keyed effect.
- The bring-to-front intent cannot accidentally trigger the deep-link handler
  (`MainActivity.kt:184`–`188`).
- `allowSelfSignedCertificates` still reaches the app's own HTTP client through
  `PreferencesManager` → `OkHttpClientProvider`, so dropping the navigation argument changes nothing
  about API connectivity (§2.3, §3.5).
- The Custom Tab offers *Advanced → Proceed* on a certificate error and the page then loads inside
  the tab — checked on a device 2026-08-30 against `https://self-signed.badssl.com/` (§2.3). This
  was the one item that could have changed the design; it did not.
- Line references, call sites, and the `ScreenshotsTestSuite` compile break were checked against the
  current files.
- Collaborator signatures used by the §4.1 fixture were read from source: `AccountRepository.getAccount()`
  returns `Flow<Resource<NcAccount>>`, `NcCookbookApiProvider.apiFlow` is a `StateFlow<NcCookbookApi?>`,
  `ClearPreferencesUseCase` is a `suspend operator fun invoke()`, and `LoginResult` wraps a non-null
  `NcAccount` — which is why `LOGIN_RESULT` exists rather than `mock()`.

Fixed during this readiness review (all in-spec defects, no design change except the first):

- **§2.6 restore behaviour was left to a default.** The restored state took `browserLaunched = false`,
  which would have made §2.5's collector re-open the Custom Tab on every return from process death.
  Now persisted explicitly as a fourth key, with the reasoning stated; covered by cases 3 and 11.
- **§4.1's fixture contradicted its own note** — the `LOGIN_ENDPOINT` sample called `.toUri()`, which
  throws on the JVM, two paragraphs above the note saying to mock `Uri`. Sample now uses the mock.
- **§4.1 was missing a `LoginResult` fixture** entirely, so cases 3 and 8 could not have been written
  as specified; `pollLoginServer` dereferences `ncAccount`.
- **§4.1 gave the wrong reason** for virtual time being shared — it is `Dispatchers.setMain` with a
  `TestDispatcher` that `runTest` picks up, not where the ViewModel is constructed.
- **`StartScreenSignInEvent.Browser` renamed to `SignIn`**, matching the `onSignInClick` rationale.
- A mangled sentence in §3.8.

Confirm during implementation, not before:

1. **needs-review — §2.4 background activity start.** Google's current documentation does not list
   an exemption that covers "start my own activity while my task's Custom Tab is in front". The
   pattern is widely used and the fallback is harmless, but whether the tab actually closes on
   Android 15/16 has to be established on a device (§4.3 step 5). If it is blocked, delete the
   `startActivity` call — do not escalate to `SYSTEM_ALERT_WINDOW` or a notification `PendingIntent`
   for this.

Accepted trade-offs, decided rather than open:

2. **Self-signed certificates lose the *silent* bypass, not the option** (§2.3). The checkbox and
   everything it controls for the app's own HTTP client stay; the browser's warning becomes one
   manual tap, now confirmed to be available. Mitigated by a FAQ entry, the manual sign-in path, and
   the "install the certificate" advice — not by code. A WebView fallback for this case was drafted
   and is no longer needed.
3. **The fix itself has no automated regression test.** No unit test can assert "the passkey sheet
   appears"; the ViewModel tests cover the state machine, token persistence, and polling, and §4.3
   covers the rest by hand.
4. **§4.1's `Uri` mocking** is the pragmatic way around `Uri.parse` on the JVM. Worth a second
   opinion at review if a different shape is preferred; it does not affect production code.

Not verified by execution: nothing here has been compiled or run — the code does not exist yet.
