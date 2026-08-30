# Tasks — Fix: sign in with a passkey (WebAuthn)

Spec: [`specs/spec/183-fix-passkey-login.md`](../../spec/183-fix-passkey-login.md)
Issue: [#183 — Unable to login using Authentik Passkey (WebAuthn)](https://github.com/lneugebauer/nextcloud-cookbook/issues/183)

## Approach

Sign-in loads the Nextcloud Login Flow v2 login URL into a bare `WebView`
(`WebViewLoginScreen.kt:98`–`:105`). A `WebView` has WebAuthn disabled by default, so
`navigator.credentials.get()` rejects and the identity provider renders "Authentication failed" —
the screenshot in the issue.

Enabling WebAuthn *inside* the WebView is a dead end for this app (spec §2.1): the app-facing
support level needs a Digital Asset Links association on every user's own server domain, the
browser-facing one is gated on Google's privileged-browser allowlist, and the documented setup pulls
a Google Play Services binding that the F-Droid `full` flavour cannot ship.

The fix is to stop using a WebView. Nextcloud's own developer manual says the v2 login URL "should
be opened in the default browser" and that the client should poll the poll endpoint until
authentication completes — which is exactly what the existing ViewModel already does. So the
transport is swapped for a Chrome Custom Tab and the polling is left alone.

Task 1 is that swap, as one vertical slice — there is no intermediate state where sign-in half
works. Tasks 2 and 3 are independent increments on top, and can be done in either order or in
parallel.

## Tasks, in dependency order

1. **[Sign in through a Custom Tab](task-1-custom-tab-sign-in.md)**
   Adds `androidx.browser`, a `Uri.openInCustomTab` extension, `BrowserLoginScreenState`,
   `BrowserLoginViewModel`, and `BrowserLoginScreen` with the `LaunchedEffect(Unit)` collector from
   spec §2.5; rewires `StartScreen`; deletes the WebView sign-in screen, its ViewModel, and
   `WebViewClient`; adds the FAQ entry and spec §4.1 unit cases 1, 2, 5, 6, 7, 8, 9.
   **This is the fix for #183.** Depends on nothing.

2. **[Return to the app when sign-in completes](task-2-return-to-app-on-success.md)**
   Adds the `FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP` bring-to-front so the Custom Tab
   closes itself instead of the user pressing Back. Kept separate because spec §2.4 is marked
   needs-review: it is a background activity start and may be blocked on Android 15/16, in which
   case this commit is reverted on its own without touching task 1.
   Depends on task 1.

3. **[Survive process death during browser sign-in](task-3-survive-process-death.md)**
   Persists the poll token to `SavedStateHandle` so a process restart resumes the same login attempt
   instead of minting a fresh token and orphaning the sign-in the user just completed. Adds spec
   §4.1 unit cases 3, 4, 10, 11 to task 1's test file.
   Depends on task 1. Independent of task 2.

## Verification notes that apply to every task

- **CI runs `bundle exec fastlane build`** (`.github/workflows/ci.yml:32`) =
  `clean → ktlintCheck → lint → test → assemble` (`fastlane/Fastfile:112`–`118`). `ktlintCheck` runs
  **before** `test`, so a formatting slip fails the build before any test runs. Run
  `./gradlew ktlintFormat` before pushing.
- **`assemble` builds `FullRelease`** (`fastlane/Fastfile:54`–`60`), so `@Preview` composables in the
  main source set **are** compile-checked by CI.
- **androidTest is never compiled by CI.** A missed `ScreenshotsTestSuite.kt` update passes CI and
  only breaks the `screenshots` lane later. Task 1 must verify locally with
  `./gradlew compileFullDebugAndroidTestKotlin`.
- **`connectedAndroidTest` does not run in CI either**, which is why the passkey fix itself is
  covered by the manual steps in spec §4.3 rather than by an instrumented test.
- **Build flavors mean there is no `testDebugUnitTest` task** — use `testFullDebugUnitTest` or `test`.

## Coordinating with PR 209 (spec 208)

[PR 209](https://github.com/lneugebauer/nextcloud-cookbook/pull/209) is open against `main` and
touches two of the same files:

- **`app/build.gradle`** — it adds the *identical* line
  `testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2'` immediately after
  `mockito-core`. Task 1 needs the same dependency. **Check whether it is already present before
  adding it**, and if it is, confirm the version is `1.10.2` and leave it alone.
- **`ScreenshotsTestSuite.kt`** — PR 209 edits the `recipeDetailScreen()` case around `:160`; task 1
  edits the `startScreen()` case at `:68`. Different hunks, no textual conflict expected, but if 209
  lands first the line numbers shift. **Locate the call site by name, not by line number.**

## Out of scope for every task

Per spec §1: no changes to `ManualLoginScreen`, no cleanup of the unused
`allowSelfSignedCertificates` argument on `ManualLoginScreenDestination`, and no retro-fitting of
`ActivityNotFoundException` handling onto the existing `Uri.openInBrowser`.

No `CHANGELOG.md` (none exists — release notes derive from commit messages) and no edits to
`fastlane/metadata/android/*/changelogs/*.txt`.
