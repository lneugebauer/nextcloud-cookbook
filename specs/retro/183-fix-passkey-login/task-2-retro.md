# Task Retro: Task 2 — Return to the app when sign-in completes

**Task spec**: `specs/tasks/183-fix-passkey-login/task-2-return-to-app-on-success.md`
**Sessions analyzed**: 1 (`52f2fd0c-b034-4248-baa4-2265c57ce585`)
**Date**: 2026-08-30

## Session Stats

| Metric | Value |
|--------|-------|
| Sessions | 1 |
| User messages (iterations) | 1 |
| Errors encountered | 2 (both `Exit code 144` — `pkill`/`kill %1` against the session's own mock server; not app or build failures) |
| Duration | 22 min (2026-08-29 22:50:44Z → 23:13:18Z) |
| Input tokens | 194 |
| Output tokens | 50,685 |
| Cache read tokens | 10,021,651 |
| Cache creation tokens | 136,492 |
| Questions asked (AskUserQuestion) | 0 |
| Subagent spawns (Task) | 0 |
| Tool calls | 99 — 95 Bash, 4 Read (all four Reads are emulator screenshots; the session ran in auto mode, so the source edit went through Bash) |
| Resulting commit | `e03e502` — *fix: close the sign-in browser tab once authentication completes* (1 file, +16/−1) |

**One user message for the whole session.** No clarification, no correction, no approval round-trip.
The 16-line code change was ~10% of the session; the other 90% was building a verification harness
for a question the spec had declared unanswerable.

## What Went Well

**The code landed verbatim.** The spec dictated an exact snippet and an exact location — the
`Authenticated` branch of the existing `LaunchedEffect(Unit)` collector, *after*
`navigator.navigate(...)`, no new effect. That is precisely what shipped. The three pieces the spec
named (`context.getActivity()`, `MainActivity`, `MainActivity.onNewIntent`) were each opened and
confirmed to exist before the edit rather than trusted from the spec.

**The spec's open question got a real answer instead of a shrug.** This is the session's defining
move. The spec said *"There is no automated verification for this task, and that is inherent"* and
handed the question to manual device testing that would happen later, if at all. The session refused
that framing: it read the app's own auth contract (`AuthApi`, `AuthRepositoryImpl`,
`LoginEndpointResponse`, `LoginResponse`, `PollDto`, `OcsDto`, `UserMetadataDto`,
`NcCookbookApiProvider`, `Constants`), stood up a Python mock of Nextcloud Login Flow v2 serving
`index.php/login/v2`, the poll endpoint, `ocs/v2.php/cloud/user` and `capabilities`, and drove the
**real** sign-in flow — real Custom Tab, real taps via `uiautomator dump` + `input tap` — on two
emulators.

The result is not an impression, it is a framework decision quoted from logcat:

- **Android 15 / API 35**: `START u0 {flg=0x24000000 cmp=…/MainActivity} with LAUNCH_MULTIPLE from
  uid 10215 (BAL_ALLOW_FOREGROUND) result code=3`. `BAL_ALLOW_FOREGROUND` is the background-activity-launch
  check *allowing* the start; `result code=3` is `START_DELIVERED_TO_TOP`, i.e. `onNewIntent` with no
  recreation. Transition log: `CustomTabActivity` CLOSE + `MainActivity` TO_FRONT. Tab closed ~3s
  after granting; app logged `route=home_screen`; Back from Home exits to the launcher exactly as
  after a normal sign-in.
- **Android 10 / API 29**: `START u0 {…} from uid 10147`, no abort. Only `am_on_resume_called`, no
  `am_on_create_called`, same pid (6525) either side — the activity was reused, not recreated.
- Both: nothing matching `background activity` in logcat, task down to `sz=1`.

That is a better answer than the manual procedure the spec asked for, and it arrived before the
change was committed rather than after.

**Two false alarms were diagnosed as harness bugs, not app bugs.** The first run looked like a
stalled poll loop — the app only reached Home after being brought forward manually. Rather than
"fixing" the app, the session timestamped the mock's request log and identified an emulator NAT
artifact: `10.0.2.2` plus HTTP keep-alive silently strands the app's poll connections once it is
backgrounded. Switching to `adb reverse` with `Connection: close` produced a clean 5s poll cadence
while the tab was on top. The API 29 run then crashed on the mock's `capabilities` payload (missing
`theming`) — again correctly attributed to the mock. Both were called out in the handoff as dead
ends, not achievements.

**The device range was checked, not assumed.** The spec asked for "one Android 13 or older". The
session looked up `minSdk 25` first to confirm an API 29 AVD was a legitimate target before booting
it.

**Honest gate reporting, again.** `ktlintCheck` passed. `assembleFullRelease` failed at
`validateSigningFullRelease` (Cryptomator keystore unmounted); the session recognised this from the
project memory file, applied the eCryptfs build-dir relocation, ran
`:app:compileFullReleaseKotlin :app:lintVitalAnalyzeFullRelease` instead, and reported the
substitution rather than claiming the gate green. It also disclosed unprompted that emulators are not
physical devices and that a phone run would close that gap.

**Environment left clean.** `adb reverse --remove-all` on both AVDs, `pm clear`, the second emulator
shut down, the mock killed. The pre-existing `fastlane/Fastfile` modification was left unstaged.

## Spec Accuracy

| Deliverable | Status | Notes |
|-------------|--------|-------|
| `startActivity(Intent(MainActivity), CLEAR_TOP or SINGLE_TOP)` in the `Authenticated` branch | as-specified | Character-for-character the spec's snippet. |
| Placed **after** `navigator.navigate(...)` | as-specified | Order preserved; branch converted to a block. |
| Extend the existing `LaunchedEffect(Unit)`, do not create a new effect | as-specified | No new effect anywhere in the file. |
| Use the existing `core/util/getActivity()` extension | as-specified | Import added; extension unchanged. |
| Do **not** add a test that only asserts the intent was constructed | as-specified | No test added — the spec's prohibition was honoured. |
| Do **not** escalate to `SYSTEM_ALERT_WINDOW` / foreground service / notification `PendingIntent` | as-specified | Never considered. |
| Kill-switch: delete the call if the start is blocked | n/a | Not triggered — the start was demonstrably allowed on both API levels. The call survives unchanged on HEAD. |
| Manual verification on **two devices** spanning Android 13-or-older and Android 15-or-newer | **modified** | Two *emulators* (API 29 / Android 10, API 35 / Android 15). Disclosed explicitly in the handoff. The BAL decision is made by the framework, so this is strong evidence — but the spec said "devices". |
| Re-run task-1 regression §4.3 step 1 (end-to-end sign-in) | **modified** | Exercised against the mock server, not a real IdP with a passkey. |
| Re-run task-1 regression §4.3 step 6 (tab dismissed mid-flow) | **dropped** | Not exercised, and not listed as skipped in the handoff. See Learning 5. |
| Record the result — Android version, device, whether the log line appeared — **in the pull request** | **dropped** | Recorded in the commit body and the session summary. No PR existed yet (it was opened two tasks later); the PR body as it stands says return-to-app verification is *still outstanding*, and the commit body was subsequently erased. See Learning 1. |
| `./gradlew ktlintCheck` passes | as-specified | Ran, passed, re-ran after cleanup. |
| `./gradlew assembleFullRelease` succeeds | **dropped** | Cannot run without the Cryptomator keystore. Substituted `compileFullReleaseKotlin` + `lintVitalAnalyzeFullRelease`, both green. See Learning 4. |
| Conventional-commit `fix:` subject, kept as its own revertible commit | **modified** | Subject and isolation correct — one file, independently revertible. But a multi-paragraph body *and* a `Co-Authored-By` trailer were added, both forbidden by the user-global commit convention. See Learning 2. |
| Explanatory comment at the call site | **added** | Six of the sixteen added lines. Not requested by the spec; carries the CLEAR_TOP/SINGLE_TOP rationale and the best-effort caveat into the source, matching the pattern task 1 established. |

**Every line of prescribed code landed exactly. Every divergence is in verification and
record-keeping.**

## Spec Gaps

**The spec named a destination for the evidence that did not exist yet.** *"Record the result … in
the pull request either way, so the question does not get re-litigated later."* At the moment this
task ran there was no pull request — PR 210 was opened in a later session, after task 3. The spec
gave no fallback, so the evidence went into the only durable place available, which the project's
commit convention forbids. It was later erased. The instruction was right about *why*; it was wrong
about *where*, because it assumed a sequencing that the task order does not produce.

**No guidance on what to do when a "needs-review" verdict comes back positive.** The spec is
detailed about the negative branch — delete the call, record it, close the task, do not escalate.
The positive branch gets one clause. Nothing says the `needs-review` marker in §2.4 should be
retired, or that the PR narrative describing the change as unverified should be updated. Both are
still stale.

## Over-Design

**None in the spec.** The prescription was one code block long and it was right.

**Arguably in the session — and it paid off.** 95 Bash calls, two booted emulators, a hand-written
mock server and roughly 20 minutes for a 16-line change is a large ratio. It is justified precisely
because the spec flagged the change as `needs-review` and revert-eligible: the entire value of the
task was the answer to "is this blocked?", not the six lines of `Intent` construction. Had the
session skipped verification, it would have shipped exactly the uncertainty the spec was trying to
resolve. The cost is worth noting only because it is the dominant cost of the session, not because
it was misspent.

## Under-Design

**The spec reasoned about the manual verification but not about who would perform it or when.** It
specified the procedure, the device range, the logcat filter and the pass/fail branches — a complete
procedure with no owner. In a session with one user message and no follow-up, "verify manually on
two devices" resolves to either "assert it works and hope" or "invent a harness". The session chose
the second, well; the spec should have anticipated that this is the choice it was forcing.

**The instruction "record it in the PR" and the project's subject-line-only commit convention were
never reconciled.** Both constraints were in force. Together they leave no in-repo place for
verification evidence at the moment the task runs. That interaction was never considered.

## Code Review & Corrections

### User Message Classification

| # | Timestamp | Intent | Summary |
|---|-----------|--------|---------|
| 1 | 2026-08-29 22:51:11Z | initial-prompt | "implement specs/tasks/183-fix-passkey-login/task-2-return-to-app-on-success.md" |

No corrections, no review feedback, no clarifying questions in either direction.

### Review Findings

| # | Finding | Root Cause | Scope | Domain |
|---|---------|-----------|-------|--------|
| 1 | Commit body plus `Co-Authored-By` trailer, both banned by the user-global CLAUDE.md. Cleaned up later by a branch-wide `filter-branch` (`bdcb015` → `e03e502`) that stripped bodies from *every* commit on the branch. | convention-miss | team | process |
| 2 | Verification evidence never reached the PR. It lived only in the commit body, which the rewrite in finding 1 deleted; PR 210's body still describes return-to-app as unverified. Surviving copies: dangling commit `bdcb015` and the session transcript. | under-design | team | process |
| 3 | Apparent "stalled poll loop" on the first emulator run. **Not a defect** — `10.0.2.2` + HTTP keep-alive strands the app's poll connections while backgrounded. Self-diagnosed; no code change. | n/a — harness artefact | — | — |
| 4 | API 29 crash on the mock's `capabilities` response. **Not a defect** — missing `theming` in the hand-written payload. Self-diagnosed; no code change. | n/a — harness artefact | — | — |

Findings 3 and 4 are the interesting ones: both looked exactly like app bugs, both were correctly
pinned on the test harness, and neither produced a speculative "fix" to production code.

## Emerged Designs

**A repeatable emulator harness for the sign-in flow.** Mock Nextcloud Login Flow v2 over plain
Python HTTP, reached via `adb reverse` (not `10.0.2.2`) with `Connection: close`, driven through the
real Custom Tab with `uiautomator dump` + `input tap`. This turns the entire auth path — including
process death, tab dismissal and the task-3 resume behaviour — into something observable without a
real Nextcloud server. The spec assumed this class of question was unanswerable without hardware;
it is not. Worth keeping as a documented harness rather than rebuilt from scratch next time.

**`adb reverse` + `Connection: close` is a hard requirement, not a preference.** With `10.0.2.2` and
keep-alive, a backgrounded app's polling *appears* to stop while the app is in fact healthy. Any
future emulator test of a background network loop in this project will hit this and mistake it for
a regression.

**Read the framework's own verdict rather than the visible outcome.** `BAL_ALLOW_FOREGROUND` and
`result code=3` in `ActivityTaskManager` answer "was the background start allowed?" and "was
`onNewIntent` delivered or was the activity recreated?" directly, instead of inferring from whether
the tab looked like it closed. Pairing that with `am_on_create_called` / `am_on_resume_called` and a
pid comparison is a clean recipe for proving activity reuse.

**Rationale comments at the call site, again.** Same pattern as task 1: the spec's reasoning about
CLEAR_TOP/SINGLE_TOP and the best-effort caveat were copied into the source, so the next reader
cannot delete the call as redundant once the spec is archived. Emergent both times — worth promoting
into the task-spec template as an explicit deliverable.

## Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | The spec required verification evidence to be recorded "in the pull request", but no PR existed when the task ran — it was opened two tasks later. The evidence went into the commit body instead, a later branch-wide `filter-branch` erased every commit body, and PR 210 still states that return-to-app verification is outstanding. A spec that demands durable evidence must name a destination that exists at the moment the task executes. | team | process | under-design |
| 2 | The commit carried a multi-paragraph body and a `Co-Authored-By` trailer, both explicitly forbidden by the user-global commit convention ("never add a `Co-Authored-By` trailer"; "default to a subject line only"). The tool-level default trailer was followed over the user's override, and the cleanup rewrite destroyed the rationale in all four commits on the branch as collateral. | team | process | convention-miss |
| 3 | The spec asserted that automated verification was "inherent[ly]" impossible and routed the question to manual device testing. A mock Login Flow v2 server, `adb reverse` and two AVDs answered it decisively in ~20 minutes, quoting the framework's own BAL decision. Specs should not declare a question unverifiable before checking whether an emulator harness can answer it. | team | process | spec-wrong |
| 4 | "Done when: `./gradlew assembleFullRelease` succeeds" appeared again, and again could not run — `validateSigningFullRelease` needs a keystore that lives in an unmounted Cryptomator vault. This is the second task in the same spec to hit it. Specs wanting release-variant compilation verified should ask for `compileFullReleaseKotlin` (plus `lintVitalAnalyzeFullRelease`). | project | tech-stack:android | spec-wrong |
| 5 | The spec required re-running task-1 regression steps §4.3 1 and 6; step 6 (tab dismissed mid-flow) was never exercised, and the handoff listed what had been verified without listing what had not. A verification summary that enumerates only successes reads as complete coverage. | team | process | baseline-miss |
