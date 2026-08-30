# Spec Retro: Fix — sign in with a passkey (WebAuthn)

**Spec**: `specs/spec/183-fix-passkey-login.md`
**Tasks**: 3 (0 corrections across all three)
**Date**: 2026-08-30

## Summary

This spec produced three implementation sessions with **zero corrections**, zero rework, and zero
clarifying questions between them — the user's only substantive interjections were one device-test
observation that turned out to be documented intermediate behaviour, and "push and open a pr". Every
line of prescribed production code landed as written. The density paid for itself: exact line
numbers, verified collaborator signatures, and citations into Compose framework internals meant the
single most subtle instruction in the spec (§2.5's `LaunchedEffect(Unit)` collector) was implemented
correctly on the first attempt by a session that had no way to rediscover the reasoning.

The spec's failures are concentrated in two places, and both are structural rather than local.
**First, verification.** The spec was excellent at deciding *what to build* and consistently weak at
deciding *how the result would be proven* — it prescribed a Gradle task that cannot run in this
checkout (twice), an evidence destination that did not exist yet, and declared a question
"inherent[ly]" unautomatable that a session answered decisively in twenty minutes. The primary
acceptance criterion for issue #183 — a passkey sign-in against a real IdP — is still unverified.
**Second, the trust boundary.** The entire spec is about moving the login URL out of a sandboxed
WebView into the user's browser, and §2 contains no decision about validating that server-supplied
URL before launch. That gap, plus a `retry()` that was specified as four key deletions rather than as
a state transition, are the two real code defects on the branch — both found by the PR review pass
twelve hours after the last task retro was written, both fixed in `cd0bf3c`.

| Metric | Value |
|--------|-------|
| Tasks | 3 |
| Total corrections | 0 |
| Tasks with 0 corrections | 3 |
| User messages (all tasks) | 7 (4 / 1 / 2) |
| Learnings by root cause (task retros) | spec-wrong: 5, under-design: 4, convention-miss: 2, baseline-miss: 2, spec-gap: 1, over-design: 0 |
| Additional spec-level learnings | 6 (see below) |
| Post-task defect fixes | 2 code (`cd0bf3c`), 1 docs (`c2867ca`) |
| PR 210 review findings | 9 raised, 3 fixed, 6 open |

## Architecture Decisions Scorecard

| # | Decision | Score | Notes |
|---|----------|-------|-------|
| 2.1 | Rejected: Credential Manager inside the WebView | **held-up** | Never revisited by any task. The rejection was argued from the AOSP javadoc for both non-default support levels plus the F-Droid constraint, and it closed off the exact dead end the issue thread was pointing at. Cost: a page of prose. Value: three sessions that never considered it. |
| 2.2 | Replace the WebView with a Chrome Custom Tab | **held-up** | Task 1 landed 14 of 15 deliverables exactly as specified in one pass. The one divergence (§3.8 strings) was in a different decision. Caveat: the design holds, but the *acceptance* — a passkey against a real IdP, §4.3 step 1 — remains unverified, so "it fixes #183" is still an argument rather than an observation. |
| 2.3 | Self-signed certificates: the option stays, the silent bypass does not | **partially-wrong** | The decision itself is right and cost nothing in code. Its mitigation is the problem: §3.9 dictates user-facing FAQ copy — "tap *Advanced → Proceed*" — that is Chrome's wording, in the same spec that states the Custom Tabs provider is "whichever browser the user has chosen" and that the check was worth repeating in Firefox and Samsung Internet. Raised on PR 210, still open. |
| 2.4 | Returning to the app: best-effort bring-to-front | **held-up** | Marked `needs-review` and shipped as revert-eligible; task 2 then proved it works, quoting `BAL_ALLOW_FOREGROUND` and `result code=3` (`START_DELIVERED_TO_TOP`) from `ActivityTaskManager` on API 29 and API 35. The kill switch never fired. The failure is bookkeeping, not design: the marker was never retired and PR 210 still describes this as unverified. |
| 2.5 | The success signal must not depend on recomposition | **held-up** | The spec's hardest instruction and its best. Implemented as written, with the framework rationale copied into a comment at `BrowserLoginScreen.kt` so it survives the spec's archival. Task 2's whole mechanism depends on this collector resuming while the activity is stopped — getting it right here is why task 2 was 16 lines. |
| 2.6 | Survive process death: persist the poll token | **partially-wrong** | The core insight is correct and genuinely earned ("with a Custom Tab the app is backgrounded for as long as an SSO round-trip takes"), and adding `browserLaunched` as a fourth key was a readiness-review catch that prevented a visible bug. But three of the four sub-decisions had defects: the restore guard names two keys for a branch that consumes three, `retry()` is specified as key deletion rather than as a state transition (shipped a live bug, fixed in `cd0bf3c`), and "the user can use the retry action" describes an affordance that does not exist in the state it applies to. |
| 2.7 | No `<queries>` element is required | **held-up** | Manifest unchanged; `ActivityNotFoundException` caught rather than `resolveActivity` pre-checked, exactly as the consequence paragraph instructed. The reasoning was corroborated against the app's own shipped `Uri.openInBrowser`, which is why it held. |
| 2.8 | New test dependency (`kotlinx-coroutines-test`) | **held-up** | Including the conditional — "PR 209 may have already added it, check first" — which task 1 resolved by checking rather than assuming. A small thing that models the right behaviour. |
| — | **Missing: validating the server-supplied login URL** | **gap** | See Spec Review #10. There is no §2 decision covering the trust-boundary change the spec's central move creates. |

## Task Breakdown Assessment

| Task | Corrections | Messages | Assessment |
|------|-------------|----------|------------|
| Task 1: Sign in through a Custom Tab | 0 | 4 | **well-sized.** Large (22 files, +497/−248) but genuinely atomic — the README's "there is no intermediate state where sign-in half works" is correct. Splitting it would have produced a branch that cannot sign in. |
| Task 2: Return to the app when sign-in completes | 0 | 1 | **well-sized.** 16 lines, kept separate purely so it could be reverted alone if the background activity start turned out to be blocked. The kill switch never fired, but the isolation was the right bet on a `needs-review` decision — and the session spent 90% of its time answering that question, which is exactly what the split was for. |
| Task 3: Survive process death | 0 | 2 | **well-sized.** 130 lines, independent of task 2, six minutes to implement. |

**Dependencies were correct in the code direction.** Tasks 2 and 3 both depend only on task 1, and
neither required work from the other. Nothing had to be done out of order.

**But there was a hidden dependency in the verification direction, and the README explicitly denied
it.** "Tasks 2 and 3 are independent increments on top, and can be done in either order or in
parallel." True of the code. False of the acceptance: task 3's primary Done-when is "kill the process
mid-sign-in and land signed in on the original token", and the only apparatus in existence capable of
driving that is the mock Login Flow v2 server task 2 had built twenty-five seconds earlier. Task 3
did not know it existed — `/clear` had discarded it and nothing in the repo recorded it — concluded
the step needed a real Nextcloud instance, and shipped unverified. Had the tasks run in the other
order, as the README says they may, task 2 would have had to build it anyway; had they run in
parallel, it would have been built twice.

**Missing tasks.** Two.

*No task owned §4.3.* The eight manual acceptance steps live in the spec, and each task spec pulled
out the one or two that touch its own change. Steps 1–4 — the reported Authentik passkey case, a
security key, password/2FA regression, self-signed end-to-end — belong to no task at all. They are
the acceptance criteria for the issue this spec exists to fix, and they have no owner, no schedule,
and are still outstanding on a PR that is otherwise finished. A breakdown that ends when the last
code task ends leaves the actual bug unverified.

*No task owned the review round.* `cd0bf3c` (two real defects) and `c2867ca` (checkout-specific paths
in a committed task doc) both landed twelve hours after task 3, outside the task structure entirely.
This is not a criticism of the fixes — it is that the task retros, written before them, describe a
branch state that no longer exists, and nothing in the breakdown anticipated that a review pass
produces work.

**Merges and splits: none needed.** Task 2 at 16 lines is the only candidate for merging into task 1,
and merging it would have destroyed the property it was split for.

## Spec Review

| # | What the spec said | What happened | What the spec should have said |
|---|-------------------|---------------|-------------------------------|
| 1 | §3.8: "Remove `error_webview_load_failed` (`:66`) … Translations in `values-*/strings.xml` are managed by Weblate; delete only the `values/` entry and let Weblate reconcile." | Following this literally leaves eight orphaned translations of a string with no default. `app/lint.xml` ignores `MissingTranslation` but **not** `ExtraTranslation`, and `lint` runs in CI's `build` lane — so the spec's instruction ships a red build. Caught only because task 1 ran `lint`, which its own Verification section did not list. | "Remove `error_webview_load_failed` from `values/strings.xml` **and from all eight `values-*/strings.xml` files**. Weblate reconciles *added* strings; a removed default leaves its translations orphaned, and `app/lint.xml` treats `ExtraTranslation` as an error (only `MissingTranslation` is ignored), so they must go in the same commit." |
| 2 | Task-1 Verification listed four Gradle checks: `ktlintCheck`, `testFullDebugUnitTest`, `compileFullDebugAndroidTestKotlin`, `assembleFullRelease`. | `lint` and `npm run docs:build` were omitted, though CI runs both — and the tasks README two documents away correctly states the lane is `clean → ktlintCheck → lint → test → assemble`. An implementer treating the list as the definition of done pushes red. Task 1 ran the pipeline anyway; that diligence is what caught #1. | "Run `bundle exec fastlane build` — the CI lane in full (`clean → ktlintCheck → lint → test → assemble`) — plus `npm run docs:build` for the FAQ change and `compileFullDebugAndroidTestKotlin` for the androidTest source set CI never compiles. The itemised list below is what each lane is expected to catch, not a substitute for running them." |
| 3 | Task 1 and task 2 both: "`./gradlew assembleFullRelease` succeeds." | Fails at `validateSigningFullRelease` in any checkout without `keystore.properties` — which is every local checkout; CI creates an empty one. Both tasks substituted `compileFullReleaseKotlin`, independently, and both reported the substitution honestly. The same defect was copied into two task specs. | "`./gradlew compileFullReleaseKotlin :app:lintVitalAnalyzeFullRelease` — this is what actually compile-checks the `@Preview` composables. Do **not** ask for `assembleFullRelease` locally: it fails at `validateSigningFullRelease` without a `keystore.properties`, which only CI provides." |
| 4 | Task 2: "Record the result — Android version, device, whether the log line appeared — in the pull request either way, so the question does not get re-litigated later." | No PR existed when task 2 ran; PR 210 was opened after task 3. The evidence went into the commit body — the one place the user's commit convention forbids — and a later branch-wide `filter-branch` erased it from all four commits. PR 210 still says return-to-app verification is outstanding. The instruction was right about *why* and wrong about *where*. | "Append the result to this task document's Verification section and commit it with the change: Android version, device, and the `ActivityTaskManager` line. If a PR is open, mirror it into the PR body. Do not put it in the commit body — the project's commit convention is subject-line only." |
| 5 | Task 2: "There is no automated verification for this task, and that is inherent." Spec §4.3: "The actual bug is only reproducible against a real IdP, so these are the acceptance steps." | Task 2 refused the framing, built a Python mock of Login Flow v2, drove the real Custom Tab through `uiautomator dump` + `input tap` on two AVDs, and answered the `needs-review` question decisively from logcat in about twenty minutes. The claim of inherent unverifiability was simply false, and it was made without checking. | "Verification here is not automatable *in CI* — `connectedAndroidTest` does not run there. It **is** answerable locally: a mock Login Flow v2 server (`index.php/login/v2`, the poll endpoint, `ocs/v2.php/cloud/user`, `capabilities`) reached over `adb reverse` drives the full flow on an emulator without a real Nextcloud. Build it before falling back to manual device testing. Only the passkey sheet itself needs a real IdP." |
| 6 | §3.4: "`init` reads `savedStateHandle["pollUrl"]` / `["pollToken"]` first; if both non-null, set `Loaded(loginUrl, …)` from the saved login URL and resume `pollLoginServer`". | The restore branch also dereferences the saved login URL to build `Loaded`, so a two-key guard crashes on a partially-populated handle. Task 3 quietly widened it to three and was right to. CodeRabbit raised the same point independently. | "If `pollUrl`, `pollToken` **and** the saved login URL are all non-null, restore `Loaded` and resume polling. If some but not all are present, clear the partial keys and fall through to `getLoginEndpoint`. The guard must cover every key the branch consumes." |
| 7 | §3.4: "`retry()` (`:64`–`68`) clears all four saved keys before re-requesting", and §4.1 case 10 asserts exactly that. | Implemented exactly as specified — and still shipped a bug. `pollLoginServer` is a self-recursive suspend loop stopped only by the `_uiState.value is Loaded` guard task 1 introduced. `retry()` produces a fresh `Loaded`, so the abandoned coroutine wakes from its 5 s `delay`, sees `Loaded`, and polls the dead token alongside the new one. The spec designed both halves and never put them in the same sentence. Case 10 passed green over the defect. Fixed in `cd0bf3c` with a tracked `pollJob`. | "Hold the polling loop in a `pollJob: Job?` field, started through a `startPolling(url, token)` helper that cancels its predecessor. `retry()` cancels `pollJob` **before** clearing the keys — clearing them only stops a *process restart* from resuming the dead token; the coroutine parked in `POLL_DELAY` is untouched and will resume against the fresh `Loaded` state. Case 10 must assert the abandoned token is polled exactly once, not merely that the keys are null." |
| 8 | §4.1: "make the fixture `Uri` a `mock<Uri>()` whose `toString()` is stubbed, since the ViewModel only stores and forwards it." | True for task 1's cases. Task 3's restore path calls `savedLoginUrl.toUri()` in production code and case 3 drives it — and `String.toUri()` is `Uri.parse()`, an Android framework call with no JVM implementation, which throws "not mocked". Solved with `mockStatic(Uri::class.java)` at no cost, but rediscovered rather than read. | Add to §4.1: "Cases that exercise the *restore* path additionally need `mockStatic(Uri::class.java).use { … }` with `Uri.parse(SAVED_LOGIN_URL)` stubbed to `LOGIN_URI` — `String.toUri()` is `Uri.parse()` and throws 'not mocked' on the JVM. The `mock<Uri>()` fixture covers only the paths that store and forward the `Uri`." |
| 9 | §4.3 step 7: "Process death (§2.6). With the tab open, enable *Don't keep activities* … complete the browser sign-in, then return." | Task 3's primary Done-when. An emulator was attached; the session concluded the step needed a real Nextcloud instance and went straight to commit. The harness that could have driven it had been built twenty-five seconds earlier in the previous session and existed nowhere the task could reach it. | Step 7 should name its apparatus: "…using the mock Login Flow v2 harness (see `specs/…/verification-harness.md`), `adb shell am kill de.lukasneugebauer.nextcloudcookbook.debug` mid-flow." And the breakdown must require that a harness built for one task is written into the repo before the next task starts. |
| 10 | **Missing: nothing in §2 decides what to do with the login URL the server returns.** §3.2's `openInCustomTab` accepts any `Uri`; §3.4 stores whatever `getLoginEndpoint` produced and hands it to the browser. | The spec's central move takes a server-controlled URL out of a sandboxed WebView and hands it to an external browser — a trust-boundary change it never names. A hostile or compromised server response could return `javascript:`, `intent://` or `file://` and the app would launch it. Raised as the only 🟠 Major finding on PR 210; fixed in `cd0bf3c`. The app already validates exactly this for user-entered URLs (`StartScreenViewModel.kt:90`, `R.string.error_invalid_protocol`), so the precedent was in the codebase the whole time. | A §2 decision: "**The login URL is now launched externally, so it must be validated.** In the WebView the URL was loaded into a component we controlled; a Custom Tab hands it to the browser as an implicit intent. `getLoginEndpoint` rejects any `loginUrl` whose scheme is not `http` or `https` — the same pair `StartScreenViewModel` validates for user-entered server URLs — and enters `Error(R.string.error_invalid_protocol)` before anything is written to the `SavedStateHandle` or the state moves to `Loaded`. Not HTTPS-only: cleartext instances are supported (`usesCleartextTraffic="true"`, `network_security_config.xml`)." |
| 11 | §2.6: "The token is valid for 20 minutes server-side; an expired one simply keeps returning `404` and the user can use the retry action. No extra expiry bookkeeping in the client." | The retry action is only reachable from `AbstractErrorScreen`, i.e. from the `Error` state. A poll returning `404` stays in `Loaded` forever (`pollLoginServer` re-enters while `_uiState.value is Loaded`), and `Loaded` renders only "Complete the sign in in your browser" plus **Open browser**. So the escape hatch the decision leans on does not exist in the state it applies to: an expired token polls silently until the ViewModel dies. Raised on PR 210, still open. | "No expiry bookkeeping *of the token* — but the `Loaded` state must be escapable. Either surface `retry()` from `BrowserLoginLayout` alongside **Open browser**, or move to `Error(R.string.error_login_expired)` after N consecutive failures so `AbstractErrorScreen`'s retry becomes reachable. As written, `Loaded` has no exit and the retry this decision relies on is only reachable from `Error`." |
| 12 | §3.9 FAQ copy: "Your browser will additionally show its own certificate warning during sign in; tap *Advanced → Proceed*." | Shipped verbatim to `docs/faqs.md`. That is Chrome's interstitial wording; Firefox and Samsung Internet differ. §2.3 of the same spec states the provider is "whichever browser the user has chosen" and that the check is "worth repeating with Firefox or Samsung Internet" — the spec knew, and still dictated single-browser copy. Raised on PR 210, still open. | "Your browser will additionally show its own certificate warning during sign in. Accept it to continue — the wording differs by browser (in Chrome it is *Advanced → Proceed*)." |
| 13 | §5: "**Ready to implement.** No `needs-research` markers remain and no decision is unresolved." — followed sixty lines later by "**needs-review — §2.4 background activity start.**" And §2.3: "Checked 2026-08-30 against `https://self-signed.badssl.com/`", written on 2026-08-29. | Two self-contradictions in the readiness section: a "nothing unresolved" claim that the same section then contradicts, and device evidence dated a day in the future. Neither misled an implementer here, but a readiness statement is the thing a reader trusts instead of re-deriving, and both were caught by an automated reviewer rather than by the spec's own review pass. | "**Ready to implement, with one open verification.** No `needs-research` markers remain. §2.4 stays `needs-review`: implementation proceeds using the documented fallback, and the question is settled on a device during task 2." And date evidence with the date it was actually taken. |
| 14 | Task 1: "Same … `pollLoginServer()` (`:124`–`141`) bodies" — while the change table below says "Change \| Polling guard `_uiState.value is BrowserLoginScreenState.Loaded`". | Contradictory instructions for one method: copy it unchanged, and change its guard. Task 1 read it correctly, but an implementer could reasonably have carried `pollLoginServerIsActive` across. Same wording in spec §3.4. | "Copy `pollLoginServer()` across **unchanged except for its guard**, which becomes `_uiState.value is BrowserLoginScreenState.Loaded`. `pollLoginServerIsActive` does not survive the rename — `Loaded` is now the only polling state." |

## Undocumented Conventions

| # | Convention | Evidence | Where to document |
|---|-----------|----------|-------------------|
| 1 | Commits are subject-line only, and never carry a `Co-Authored-By` (or any attribution) trailer. | Task 2 Learning 2 and task 3 Learning 3 — the same miss in two consecutive sessions twenty-five seconds apart. Cleanup required a branch-wide `filter-branch`, which destroyed the verification evidence in all four commit bodies as collateral (task 2 Learning 1). | **Already documented** in `~/.claude/CLAUDE.md`, and missed twice anyway — the tool-level default trailer keeps winning over the user's override. Documenting it again will not help. Enforce it: a `commit-msg` hook rejecting `Co-Authored-By` and multi-line bodies, or a line in the project `CLAUDE.md` where it is read closer to the commit. |
| 2 | There is no `testDebugUnitTest` task — the project is flavored; use `testFullDebugUnitTest`. | Root `CLAUDE.md` lists `./gradlew testDebugUnitTest` under **Testing**. It does not exist. Task 3 avoided burning a cycle on it only because its task spec carried an explicit correction; the tasks README carries the same warning. Two spec documents exist to work around one wrong line. | `CLAUDE.md` → Testing. This is not an undocumented convention, it is an actively wrong one: replace `./gradlew testDebugUnitTest` with `./gradlew testFullDebugUnitTest`. |
| 3 | `assembleFullRelease` cannot run in a local checkout — `validateSigningFullRelease` needs a `keystore.properties` that only CI creates. Use `compileFullReleaseKotlin` (+ `lintVitalAnalyzeFullRelease`) to compile-check the release variant. | Task 1 Learning 4 and task 2 Learning 4 — hit twice in the same spec, discovered independently both times. Currently lives only in Claude's private memory. | `CLAUDE.md` → Build, next to `./gradlew assembleRelease`. |
| 4 | `app/lint.xml` ignores `MissingTranslation` but treats `ExtraTranslation` as an error: removing a default string requires removing all eight `values-*` translations in the same commit. Weblate reconciles additions, not removals. | Task 1 Learning 1 — the only place the spec told the implementer to do something that could not work. | `CLAUDE.md` → Code Quality, or a short note in `app/lint.xml`. |
| 5 | CI is `bundle exec fastlane build` = `clean → ktlintCheck → lint → test → assemble`, `ktlintCheck` runs *before* `test`, `assemble` builds `FullRelease` (so `@Preview` composables are compile-checked), and neither `androidTest` nor `connectedAndroidTest` is ever compiled or run. | Task 1 Learning 2 — the task's own verification list omitted two lanes CI runs. The tasks README documents all of this correctly; `CLAUDE.md` does not, so every new spec has to rediscover it. | `CLAUDE.md` → Testing / Code Quality. Promote it out of the per-spec README. |
| 6 | Committed spec and task documents describe the repository, not one checkout — no local paths, no machine-specific workarounds. | PR 210 review on `specs/tasks/…/README.md:63`–`69`: the task plan carried `/home/lukas`, eCryptfs and a 143-byte filename limit into four documents, and would have sent other contributors to an unnecessary `/tmp` relocation. Stripped in `c2867ca`. | Currently only in Claude's private memory ("Keep local setup out of specs"). Belongs in the spec-authoring skill or `CLAUDE.md`, since it is a property of the repo, not of one assistant's context. |
| 7 | Emulator testing of the app's background polling requires `adb reverse` with `Connection: close` — `10.0.2.2` plus HTTP keep-alive silently strands the app's poll connections once it is backgrounded, and a healthy app looks like a stalled loop. | Task 2's first emulator run: diagnosed as a harness artefact rather than an app bug, but only after timestamping the mock's request log. Anyone testing a background network loop here will hit it. | Alongside the harness itself (see Spec-Level Learning 1) — this is the single fact that makes the harness usable. |

## What Went Well

**Three sessions, zero corrections.** Not a single user message across the whole spec was classified
`correction`. The one review-feedback message (task 1: "the tab did not dismiss") was answered by
pointing at the task-2 document — the behaviour was documented, deliberate, and correctly out of
scope for the commit in front of the user. No rework on any file in any session.

**The hardest instruction in the spec was the one that landed most cleanly.** §2.5 asks for a
`LaunchedEffect(Unit)` collector *against* the codebase's own `LaunchedEffect(uiState)` idiom, for a
reason (`WindowRecomposer.pauseCompositionFrameClock` on `ON_STOP`) that is invisible from the call
site and reads like something to simplify away. It was implemented as prescribed, and the reasoning
was carried into a comment in `BrowserLoginScreen.kt` so the next reader cannot undo it. Task 2's
entire mechanism depends on that property holding.

**Prescription was vindicated, not punished.** The obvious reading of the strings failure is "the
spec was too detailed". It is the opposite: the single instruction the spec got wrong is the single
place it reasoned from a general principle ("translations are Weblate-managed") without checking the
local config. Everywhere it cited a file, a line, or a framework source, it was right. Unverified
prescription was the problem; prescription was not.

**§2.1 earned its length by never being mentioned again.** A page arguing why *not* to do the thing
the issue thread suggested meant three sessions never spent a minute on it.

**Sessions treated the verification list as a floor.** Task 1 ran `lint` and `npm run docs:build`
though neither was listed, and caught the spec's one unworkable instruction before the user saw it.
Task 2 rejected "inherently unverifiable" and produced a framework-level answer to a `needs-review`
question. Task 3 parsed the JUnit XML to prove `tests="11" failures="0"` by name rather than
asserting tests pass. In all three, the interesting work happened past the edge of what was asked.

**Honest reporting throughout, including of gaps.** Every substitution was disclosed:
`compileFullReleaseKotlin` for `assembleFullRelease` (twice), emulators rather than physical devices,
and — in task 3 — a flat statement in both the handoff and the PR body that the process-death
acceptance had not been observed. No session claimed a gate it had not passed. (Task 2's summary
did enumerate only what it had verified, omitting the regression step it skipped — the one place this
slipped.)

**Two convincing false alarms were pinned on the harness, not the app.** Task 2's apparent stalled
poll loop and its API 29 crash both looked exactly like production defects. Neither produced a
speculative fix; both were diagnosed, disclosed as dead ends, and left as harness bugs.

**The readiness review caught real defects before implementation started.** The §2.6 `browserLaunched`
fourth key — without which every restore from process death would have re-opened the Custom Tab — was
found in review, not in code. So was a §4.1 fixture that contradicted its own note two paragraphs
later, and a missing `LoginResult` fixture without which two prescribed test cases could not have
been written.

**Emergent improvements over the spec, all of them keepers:** rationale comments left at the call
site (three times, unprompted, and now the reason the `startActivity` call and the `LaunchedEffect`
key cannot be "cleaned up" later); named `SavedStateHandle` key constants replacing the spec's string
literals, turning a typo from a silent restore failure into a compile error; the three-key restore
guard; and reading `BAL_ALLOW_FOREGROUND` / `result code=3` / `am_on_resume_called` out of logcat to
get the framework's own verdict rather than inferring from whether the tab looked closed.

## Spec-Level Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | Task 2 built a mock Login Flow v2 harness that answered a question the spec called unanswerable; task 3 started twenty-five seconds later, needed exactly that harness for its primary Done-when, could not see it, and shipped unverified. Verification apparatus built inside a task is invisible to every later task unless it is written into the repo. A task that builds one must have "commit the harness and reference it from the remaining task specs" as a deliverable, before the next task starts. | team | process | spec-gap |
| 2 | The breakdown assigned every code deliverable and no acceptance criterion. §4.3's eight manual steps stayed in the spec; each task pulled out the one or two touching its own change, and steps 1–4 — the reported passkey case, security key, password/2FA regression, self-signed end-to-end — belong to no task, have no owner, and are still outstanding. The acceptance criteria for the issue a spec exists to fix have to appear in the task breakdown, or the spec completes with its own premise untested. | team | process | under-design |
| 3 | Both real code defects on this branch (the un-cancelled poll job, the unvalidated login URL scheme) were found by the PR review pass twelve hours after the last task retro, and the docs defect with them. The breakdown ends at the last code task, so review-response work has no task, no retro, and no place in the plan — and the three task retros now describe a branch state that no longer exists. | team | process | spec-gap |
| 4 | `assembleFullRelease` was copied into two of three task specs as a Done-when and failed in both, for a reason already recorded in project memory. A defect in a spec's verification boilerplate propagates to every task derived from it, and each task rediscovers it independently — verification commands belong in one place the tasks reference, not copied per task. | project | process | spec-wrong |
| 5 | §2.4 was correctly marked `needs-review`, task 2 resolved it decisively, and nothing closed the loop: the marker still reads `needs-review`, the "Confirm during implementation" item is still open, and PR 210 still tells a reviewer that return-to-app is unverified. A spec that ships open questions needs a step that retires them when they are answered, or the answer is lost and the doubt outlives it. | team | process | under-design |
| 6 | Every §2 decision reasons about capability — what the Custom Tab can do that the WebView could not — and none reasons about trust. Moving a server-supplied URL from a component we control to an external browser launch is a boundary change, and the spec never names it; the app's own precedent for validating exactly that URL shape (`StartScreenViewModel`, `error_invalid_protocol`) was in the codebase and went uncited. When a spec's central move changes who controls what the app hands to the system, that belongs in §2 as its own decision. | project | tech-stack:android | spec-gap |
