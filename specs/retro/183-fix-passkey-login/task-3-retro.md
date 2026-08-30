# Task Retro: Task 3 — Survive process death during browser sign-in

**Task spec**: `specs/tasks/183-fix-passkey-login/task-3-survive-process-death.md`
**Sessions analyzed**: 1 (`bee8ffed-054a-4c13-a5cc-e95297096d7d`)
**Date**: 2026-08-30

## Session Stats

| Metric | Value |
|--------|-------|
| Sessions | 1 |
| User messages (iterations) | 2 |
| Errors encountered | 0 |
| Duration | 6 min (2026-08-29 23:13:43Z → 23:19:49Z) |
| Input tokens | 56 |
| Output tokens | 21,896 |
| Cache read tokens | 1,853,102 |
| Cache creation tokens | 61,506 |
| Questions asked (AskUserQuestion) | 0 |
| Subagent spawns (Task) | 0 |
| Tool calls | 26 — all Bash (the session ran in auto mode, so both source edits went through Bash) |
| Files with >3 edits (rework) | none |
| Resulting commit | `09c6612` — *fix: resume browser sign-in after the app is killed in the background* (2 files, +130/−7) |

The fastest of the three task sessions: six minutes, two user messages, no corrections, no
clarifying questions, no errors, no rework on any file. The second message was `push and open a pr`
— the implementation itself took one prompt.

## What Went Well

**The spec's `SavedStateHandle` design transferred to code almost verbatim.** Four keys, written in
`getLoginEndpoint`, read in `init`, cleared in `retry()`, with the login URL stored as a `String`
and `toUri()`d on read exactly as §3.4 prescribed. The spec had already done the hard thinking —
that `browserLaunched` is a fourth key and not an afterthought, that restoring it as `false` would
make task 1's `LaunchedEffect(Unit)` collector re-open the tab on top of one the user may already
have open — and the implementation simply executed it.

**The spec's build-tooling correction paid off immediately.** The task spec warns: *"There is no
`testDebugUnitTest` task — flavors."* The root `CLAUDE.md` lists `./gradlew testDebugUnitTest` under
Testing, which does not exist in this flavored project. The session ran `testFullDebugUnitTest` and
never burned a cycle on the wrong task name.

**Verification was actually run and actually read.** Not "tests should pass" — `ktlintFormat` then
`ktlintCheck` (exit 0, clean), then a targeted run of `BrowserLoginViewModelUnitTest`, then the
result XML parsed to confirm `tests="11" skipped="0" failures="0" errors="0"` with the four new case
names present, then the full suite (61 tests, 0 failures). The spec asked for eleven passing cases
and the session proved eleven, by name.

**The recorded eCryptfs/`kapt` workaround was applied unprompted.** The session wrote a Gradle init
script relocating `buildDirectory` out of the encrypted home before running tests, from the stored
memory rather than by rediscovering the filename-too-long failure.

**The unverified half was reported as unverified.** The session's closing report and the PR body both
state plainly that manual steps 3–6 are outstanding and why (no real Nextcloud instance to sign into).
No claim that the process-death fix had been observed working. See Learning 5 for what should have
happened instead — but the honesty of the report is not in question.

## Spec Accuracy

| Deliverable | Status | Notes |
|-------------|--------|-------|
| `getLoginEndpoint` writes `pollUrl`, `pollToken`, login URL as `String` before entering `pollLoginServer` | **as-specified** | Written in the `Resource.Success` branch, immediately before the `Loaded` update |
| `init` reads saved keys first; if present, resume `pollLoginServer` without calling `getLoginEndpoint` | **modified** | Spec said resume "if **both** `pollUrl`/`pollToken` are non-null". The implementation requires **three** — `savedLoginUrl` too — which is the correct reading: the restore branch calls `savedLoginUrl.toUri()` to build `Loaded`, so the spec's two-key guard would have dereferenced a null login URL. See Learning 2 |
| `init` restores `browserLaunched` from the handle, defaulting to `false` | **as-specified** | `savedStateHandle[KEY_BROWSER_LAUNCHED] ?: false` |
| `onBrowserLaunched()` / `onOpenBrowserClick()` also write `browserLaunched` | **as-specified** | `true` and `false` respectively, written alongside the existing state update |
| `retry()` clears all four keys before re-requesting | **as-specified** | Implemented exactly as written — and exactly as written was incomplete. The four `remove` calls landed; the poll coroutine already parked in its 5 s `delay` did not stop. Fixed 12 h later in `cd0bf3c`. See Learning 1 |
| Store login URL as `String`, `toUri()` on read | **as-specified** | Matching `LoginEndpointResponse.toLoginEndpointResult` as the spec instructed; `androidx.core.net.toUri` imported |
| No expiry bookkeeping | **as-specified** | None added |
| Case 3 `init_withSavedPollToken_resumesPollingWithoutNewEndpointRequest` | **as-specified** | Asserts `never()).getLoginEndpoint(any())`, `tryLogin(SAVED_POLL_URL, SAVED_TOKEN)`, and `browserLaunched == true`. Required a `mockStatic(Uri::class.java)` the spec did not anticipate — see Emerged Designs |
| Case 4 `getLoginEndpoint_onSuccess_persistsPollTokenToSavedStateHandle` | **as-specified** | Asserts `pollToken`, `pollUrl`, and additionally `loginUrl` |
| Case 10 `retry_clearsSavedPollTokenAndRequestsNewEndpoint` | **as-specified** | Asserts the keys null and a second `getLoginEndpoint` — the two things the spec named, and nothing about the abandoned poll loop, which is why Learning 1 shipped green |
| Case 11 `onBrowserLaunched_persistsFlagToSavedStateHandle` | **as-specified** | `true` after `onBrowserLaunched()`, `false` after `onOpenBrowserClick()` |
| Append to the existing test file, reuse its fixture and constants | **as-specified** | No second file, fixture untouched; three new constants (`SAVED_POLL_URL`, `SAVED_TOKEN`, `SAVED_LOGIN_URL`) added alongside the existing ones |
| Cases 1 and 2 from task 1 still pass | **as-specified** | 11/11 green, including case 1's proof that the unsaved path still calls `getLoginEndpoint` |
| Verification 1 — `ktlintFormat` then `ktlintCheck` | **as-specified** | Exit 0, no output |
| Verification 2 — `testFullDebugUnitTest`, all eleven cases | **as-specified** | 11/11 in the target class, 61/61 across the suite |
| Verification 3–6 — manual process-death test on a device | **dropped** | `adb devices` confirmed `emulator-5554` attached, then the session went straight to commit. Needs a real Nextcloud + IdP to sign into. This is the task's primary "Done when" criterion. See Learning 5 |
| Commit: conventional `fix:` subject | **modified** | Subject correct and matches the spec's suggested wording. A multi-paragraph body and a `Co-Authored-By` trailer were also added, both forbidden by the user-global convention; stripped later by the branch-wide rewrite that turned `8dee3a9` into `09c6612`. See Learning 3 |
| Named key constants in the companion object | **added** | `KEY_URL`, `KEY_POLL_URL`, `KEY_POLL_TOKEN`, `KEY_LOGIN_URL`, `KEY_BROWSER_LAUNCHED` — the spec used string literals throughout. Also converted the pre-existing `savedStateHandle["url"]` read |
| `savedStateHandle` promoted to `private val` | **added** | Implied but never stated by the spec; required now that the handle is written as well as read |

## Spec Gaps

**The manual verification steps assumed an environment nobody had established.** The task's Verification
section (and spec §4.3 step 7) put the entire process-death acceptance behind a device signing into a
real Nextcloud instance. The previous session — task 2, which ended 25 seconds before this one started
— had built a mock-server harness on the emulator for exactly this kind of question. Nothing in the task
spec pointed at it, and `/clear` had taken it out of context. The session concluded the steps needed a
real server and stopped. See Learning 5.

**Nothing else.** For a task this mechanical the spec was close to complete: it named the four keys,
the exact write sites, the `String`/`toUri()` decision with the precedent to copy, the four test case
names with their assertions, and the correct Gradle task. The two defects below are precision failures
inside what the spec did specify, not omissions.

## Over-Design

None. The spec explicitly ruled out expiry bookkeeping ("the token is valid for 20 minutes server-side;
an expired one simply keeps returning `404`"), and the implementation added none. Four keys, four
`remove` calls, one `when` branch.

## Under-Design

**`retry()` was specified as a list of key deletions, not as a state transition.** §3.4 gives
`retry()` one row — "clears all four saved keys before re-requesting" — and §4.1 case 10 asserts
exactly that. Neither considers that `pollLoginServer` is a self-recursive suspend loop parked in a
5 s `delay`, stopped only by the `_uiState.value is Loaded` guard that task 1 introduced. `retry()`
sets `Initial` and then a fresh `Loaded`, so the old loop wakes up, sees `Loaded`, and keeps polling
the abandoned token alongside the new one. The spec designed both halves — the state guard in task 1,
the retry semantics here — and never put them in the same sentence.

**The `init` restore guard was specified with the wrong arity.** "If **both** are non-null" names two
keys for a branch that consumes three. Caught during implementation; see Learning 2.

## Code Review & Corrections

### User Message Classification

| # | Timestamp | Intent | Summary |
|---|-----------|--------|---------|
| 1 | 2026-08-29T23:13:47Z | initial-prompt | `implement specs/tasks/183-fix-passkey-login/task-3-survive-process-death.md` |
| 2 | 2026-08-29T23:18:57Z | instruction | `push and open a pr` |

No corrections, no clarifications, no review feedback, no approval round-trips. The user did not
intervene in the implementation at all.

### Review Findings

| # | Finding | Root Cause | Scope | Domain |
|---|---------|-----------|-------|--------|
| 1 | `retry()` cleared the saved keys but left the in-flight poll coroutine alive; a poll parked in its `delay` woke on the new `Loaded` state and kept hammering the abandoned token. Fixed in `cd0bf3c` with a `pollJob` field, a `startPolling()` helper that cancels its predecessor, and `pollJob?.cancel()` in `retry()` — plus the regression test `retry_cancelsThePreviousPollingLoop` | under-design | project | tech-stack:kotlin |
| 2 | Commit body and `Co-Authored-By` trailer added against the user-global convention — a verbatim repeat of task 2's Learning 2, in the very next session | convention-miss | team | process |

Both were found after the session, not during it: finding 1 by the later review pass on PR #210,
finding 2 by the branch-wide history rewrite.

## Emerged Designs

**`mockStatic(Uri::class.java)` for the restore path.** The spec prescribed `savedLoginUrl.toUri()`
in production code and a case-3 fixture pre-seeded with a login URL string, without noting that
`String.toUri()` is `Uri.parse()` — an Android framework call with no JVM implementation, which
throws "not mocked" under a plain unit test. The session recognised this and wrapped the test in
`mockStatic(Uri::class.java).use { … }` with `Uri.parse(SAVED_LOGIN_URL)` stubbed to the existing
`LOGIN_URI` mock, keeping the fixture the spec asked to reuse. The comment it left —
*"`String.toUri()` is `Uri.parse()`, an Android framework call with no JVM implementation"* — is the
line the spec should have carried.

**Key constants instead of string literals.** The spec wrote `savedStateHandle["pollUrl"]` throughout.
The implementation lifted all five keys into `private const val` entries in the companion object,
including the pre-existing `"url"` read. With the same key strings now appearing in four methods,
this is the difference between a typo being a compile error and a typo being a silent failure to
restore. Worth making the default in future `SavedStateHandle` specs.

**A three-key restore guard.** Discussed above under Spec Accuracy — the implementation's guard is
strictly better than the spec's and should be what a follow-up spec reads.

## Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | `retry()` was implemented exactly as specified — clear four keys, re-request — and still shipped a bug, because neither the spec nor its case-10 test considered the poll coroutine already parked in a 5 s `delay`. The old loop resumed against the abandoned token. When a spec adds a restart path to a component whose background work is stopped by a *state guard* rather than a job handle, the spec has to say what happens to the work in flight, and the test has to assert it stopped | project | tech-stack:kotlin | under-design |
| 2 | The spec's restore guard said "if **both** `pollUrl`/`pollToken` are non-null" for a branch that also dereferences the saved login URL; implementing it literally would have crashed on a partially-populated handle. The implementation quietly required all three. A spec that enumerates keys should state the guard over the same set it consumes | project | tech-stack:kotlin | spec-wrong |
| 3 | The commit carried a multi-paragraph body and a `Co-Authored-By` trailer, both explicitly forbidden by the user-global `CLAUDE.md` — the same miss as task 2's Learning 2, repeated in the session that started 25 seconds later. The tool-level default trailer keeps winning over the user's override; recurrence across consecutive sessions means the convention needs to be enforced somewhere other than a document Claude has already read | team | process | convention-miss |
| 4 | The spec prescribed `toUri()` in production code *and* a unit test exercising that path, without noting that `Uri.parse` is unmocked on the JVM. The session solved it with `mockStatic` at no cost, but a spec that specifies both an Android framework call and the test that drives it should name the mocking requirement rather than leaving it to be discovered | project | tech-stack:android | under-design |
| 5 | The task's primary "Done when" — kill the process mid-sign-in and land signed in on the original token — went unverified, and the PR was opened saying so. The emulator was attached and the previous session had built a mock-server sign-in harness that could have driven it, but that harness lived only in a context `/clear` had discarded and was not recorded anywhere the task spec or memory could return it. Verification infrastructure built for one task in a spec has to be written down before the next task starts, or every task after it re-inherits "needs a real server" | project | process | spec-gap |
