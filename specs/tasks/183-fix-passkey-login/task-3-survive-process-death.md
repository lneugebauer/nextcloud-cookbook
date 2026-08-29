# Task 3 — Survive process death during browser sign-in

**Spec:** `specs/spec/183-fix-passkey-login.md` — read §2.6 in full, §3.4 (the ViewModel change
table), §4.1 cases 3, 4, 10, 11, and §4.3 step 7.
**Issue:** [#183 — Unable to login using Authentik Passkey (WebAuthn)](https://github.com/lneugebauer/nextcloud-cookbook/issues/183)

**Dependencies:** [`task-1-custom-tab-sign-in.md`](task-1-custom-tab-sign-in.md) must be merged
first. It creates
`app/src/main/java/de/lukasneugebauer/nextcloudcookbook/auth/presentation/browser/BrowserLoginViewModel.kt`
and
`app/src/test/java/de/lukasneugebauer/nextcloudcookbook/auth/presentation/browser/BrowserLoginViewModelUnitTest.kt`
with the fixture and cases 1, 2, 5, 6, 7, 8, 9. **Append** cases 3, 4, 10, 11 to that existing test
file — do not create a second file and do not rewrite the fixture.

**Independent of** [`task-2-return-to-app-on-success.md`](task-2-return-to-app-on-success.md);
either order is fine.

## Goal

A user signs in through the Custom Tab, the app's process is reclaimed while backgrounded, and on
return the app resumes polling **the same** login token instead of starting a fresh attempt.

**Why this matters now and did not before.** With the in-app WebView the app was foreground for the
whole sign-in. With a Custom Tab it is backgrounded for as long as an SSO round-trip takes — minutes,
with a passkey prompt and possibly 2FA — which is exactly when Android reclaims processes. After
task 1, a restart re-runs `init` → `getLoginEndpoint` and mints a **fresh** token, orphaning the
login the user just completed against the old one. They would authenticate successfully in the
browser and still land back on the sign-in screen.

## What to implement

All in `BrowserLoginViewModel.kt`. `SavedStateHandle` is already injected — this task starts writing
to it as well as reading from it.

Four keys: `pollUrl`, `pollToken`, the login URL, and `browserLaunched`.

| Change | Detail |
| --- | --- |
| `getLoginEndpoint` | On success, write `pollUrl`, `pollToken`, and the login URL **as a `String`** into `savedStateHandle` before entering `pollLoginServer` |
| `init` | Read `savedStateHandle["pollUrl"]` / `["pollToken"]` first. If **both** are non-null, set `Loaded(loginUrl, browserLaunched = savedStateHandle["browserLaunched"] ?: false)` from the saved login URL and resume `pollLoginServer` directly — do **not** call `getLoginEndpoint` |
| `onBrowserLaunched()` / `onOpenBrowserClick()` | Also write `browserLaunched` into `savedStateHandle`, so the flag survives process death |
| `retry()` | Clear all four keys before re-requesting, otherwise retry resumes a dead token forever |

Store the login URL as a `String` and `toUri()` it on read — matching how
`LoginEndpointResponse.toLoginEndpointResult` already converts (`LoginEndpointResponse.kt:20`).

**Persisting `browserLaunched` is not incidental — it is the point of the fourth key.** Without it
the restored state defaults to `false` and task 1's `LaunchedEffect(Unit)` collector re-opens the
Custom Tab the instant the screen comes back: a surprise browser launch on top of a tab the user may
already have open, and a visible flash in the common case where they *have* finished signing in and
the next poll is about to succeed. Restoring it as `true` lands the user on the waiting screen with
the **Open browser** button available, which is predictable in both directions.

No expiry bookkeeping. The token is valid for 20 minutes server-side; an expired one simply keeps
returning `404` and the user can use the existing retry action.

## Tests

Append to the existing `BrowserLoginViewModelUnitTest.kt`, reusing its fixture, its `@Before`
`Dispatchers.setMain(StandardTestDispatcher())` setup, and its `LOGIN_ENDPOINT` / `LOGIN_RESULT`
constants. Spec §4.1 numbering:

**3. `init_withSavedPollToken_resumesPollingWithoutNewEndpointRequest`**
- Fixture: `SavedStateHandle` pre-seeded with `url`, `pollUrl`, `pollToken`, the login URL, and
  `browserLaunched = true`
- Assert: `verify(authRepository, never()).getLoginEndpoint(any())`;
  `verify(authRepository).tryLogin(SAVED_POLL_URL, SAVED_TOKEN)`; state is `Loaded` with
  `browserLaunched == true` — i.e. the collector will not re-open the tab.

**4. `getLoginEndpoint_onSuccess_persistsPollTokenToSavedStateHandle`**
- Assert: `savedStateHandle.get<String>("pollToken") == "token-1"` and `"pollUrl"` matches.

**10. `retry_clearsSavedPollTokenAndRequestsNewEndpoint`**
- Assert: `savedStateHandle.get<String>("pollToken") == null` immediately after `retry()`, then a
  second `getLoginEndpoint` call.

**11. `onBrowserLaunched_persistsFlagToSavedStateHandle`**
- Act: `onBrowserLaunched()`; assert `savedStateHandle.get<Boolean>("browserLaunched") == true`.
  Then `onOpenBrowserClick()` flips it back to `false`. Case 3 covers the read side; this covers the
  write side.

Cases 1 and 2 from task 1 must still pass — case 1 asserts the *unsaved* path still calls
`getLoginEndpoint`, which is what proves the new branch is conditional rather than unconditional.

## Verification

Automated:

1. `./gradlew ktlintFormat` then `./gradlew ktlintCheck`.
2. `./gradlew testFullDebugUnitTest` — all eleven cases pass. **Relocate the build directory to
   `/tmp` first**; this checkout is on eCryptfs and `:app:kaptFullDebugKotlin` otherwise fails on a
   filename-length error unrelated to any change. See the README's verification notes. There is no
   `testDebugUnitTest` task — flavors.

Manual (spec §4.3 step 7), on a device:

3. Start sign-in so the Custom Tab is open. Enable *Don't keep activities* in developer options, or
   run `adb shell am kill de.lukasneugebauer.nextcloudcookbook.debug` (the debug build has the
   `.debug` `applicationIdSuffix`).
4. Complete the sign-in in the browser, then return to the app.
5. The app resumes polling the **same** token and signs in — it does not restart the flow, and it
   does not re-open the browser tab on its way there.
6. Re-run spec §4.3 step 6 (tab dismissed mid-flow → **Open browser** re-opens the same login URL) to
   confirm the `browserLaunched` write did not break the reopen affordance.

## Done when

- Killing the process mid-sign-in and returning results in a successful sign-in on the original
  token (verification step 5).
- All eleven unit cases pass.
- Returning after process death does **not** spontaneously re-open the browser.

## Commit

Conventional-commit `fix:` subject, e.g.
`fix: resume browser sign-in after the app is killed in the background`.
