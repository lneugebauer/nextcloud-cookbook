# Task 2 — Return to the app when sign-in completes

**Spec:** `specs/spec/183-fix-passkey-login.md` — read §2.4 in full, plus §2.5 for why the effect is
shaped the way it is, and §4.3 step 5.
**Issue:** [#183 — Unable to login using Authentik Passkey (WebAuthn)](https://github.com/lneugebauer/nextcloud-cookbook/issues/183)

**Dependencies:** [`task-1-custom-tab-sign-in.md`](task-1-custom-tab-sign-in.md) must be merged
first. It creates
`app/src/main/java/de/lukasneugebauer/nextcloudcookbook/auth/presentation/browser/BrowserLoginScreen.kt`
with the `LaunchedEffect(Unit)` collector this task extends. Do not create a new effect — add to the
existing `Authenticated` branch.

**Independent of** [`task-3-survive-process-death.md`](task-3-survive-process-death.md); either
order is fine.

## Goal

After task 1, a successful sign-in leaves the Custom Tab on top: the app has already navigated to
Home underneath, but the user has to press Back to see it. This task closes the tab automatically.

**Read §2.4 before starting — this task is marked needs-review and may end up reverted.** That is
the reason it is a separate commit.

## What to implement

One addition, in the `Authenticated` branch of the collector in `BrowserLoginScreen.kt`, **after**
the existing `navigator.navigate(...)` call:

```kotlin
context.getActivity()?.startActivity(
    Intent(context, MainActivity::class.java).addFlags(
        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP,
    ),
)
```

`context.getActivity()` is the existing extension in
`core/util/ContextGetActivityExtension.kt`. `MainActivity` is
`de.lukasneugebauer.nextcloudcookbook.core.presentation.MainActivity`.

Why this shape:

- `CustomTabsIntent.launchUrl` was called with the Activity context in task 1, so the browser
  activity is in the app's own task, directly above `MainActivity`. `CLEAR_TOP` finishes everything
  above `MainActivity` — which is the Custom Tab.
- `SINGLE_TOP` alongside it makes the system deliver `onNewIntent` to the existing `MainActivity`
  instead of destroying and recreating it, which preserves the Compose navigation back stack.
  `MainActivity` already implements `onNewIntent` (`MainActivity.kt:113`).
- There is no public API to close a Custom Tab; this is the standard pattern.

**Order matters: navigate first, then start the activity.** `NavController` state updates directly
and needs no frame, so by the time the app comes forward it is already rendering Home.

**This intent cannot trigger the deep-link path** — verified: `NextcloudCookbookApp` only calls
`navController.handleDeepLink(intent)` when `intent?.data != null && intent.action != ACTION_MAIN`
(`MainActivity.kt:184`–`188`), and `Intent(context, MainActivity::class.java)` has neither data nor
action.

## What could go wrong, and what to do about it

While the Custom Tab is on top the app has no visible window, so this is a **background activity
start**. The current
[Restrictions on starting activities from the background](https://developer.android.com/guide/components/activities/background-starts)
page lists eight exemptions and **none of them covers "the app has an activity in the back stack of
the foreground task"** — that exemption appears in older revisions and in secondary sources, but not
in what Google publishes today, and Android 15/16 tightened the rules further.

A blocked start **fails silently**: no exception, no return value. The system logs
`Background activity launch blocked!` under the `ActivityTaskManager` tag.

**If verification shows it is blocked, delete the `startActivity` call and close this task.** Do not
escalate to `SYSTEM_ALERT_WINDOW`, a foreground service, or a notification `PendingIntent` — none of
those is worth it to skip one Back press. Record the result (Android version, device, whether the log
line appeared) in the pull request either way, so the question does not get re-litigated later.

The design degrades gracefully by construction: navigation to Home happens independently of the
activity start, so a blocked start means the user presses Back once and is already signed in and on
Home. No error, no repeated sign-in.

## Verification

There is no automated verification for this task, and that is inherent — no unit test can observe
Android task ordering, and `connectedAndroidTest` does not run in CI. Do not add a test that only
asserts the intent was constructed; it would prove nothing about whether the start succeeds.

Manual (spec §4.3 step 5), on **at least two devices spanning the range**: one Android 13 or older
and one Android 15 or newer.

1. Sign in through the Custom Tab and complete authentication.
2. Confirm the tab closes on its own and the app comes forward on Home, signed in.
3. Confirm the navigation back stack is intact — pressing Back from Home behaves as it does after a
   normal sign-in, and the app was not recreated from scratch.
4. If the tab does **not** close, check logcat:
   `adb logcat -s ActivityTaskManager | grep -i "background activity"`. Confirm the fallback: one
   Back press lands on Home, signed in.

Re-run the task-1 regression steps that touch this path — spec §4.3 steps 1 and 6 — to confirm
nothing regressed.

## Done when

- On every device tested, either the tab closes automatically, or it is confirmed blocked, the
  `startActivity` call is removed, and the PR records the evidence.
- `./gradlew ktlintCheck` passes and `./gradlew assembleFullRelease` succeeds. (If you run unit
  tests, relocate the build directory to `/tmp` first — this checkout is on eCryptfs and kapt
  otherwise fails on a filename-length error. See the README's verification notes.)

## Commit

Conventional-commit `fix:` or `feat:` subject, e.g.
`fix: close the sign-in browser tab once authentication completes`. Keep it as its own commit so it
can be reverted independently of the #183 fix.
