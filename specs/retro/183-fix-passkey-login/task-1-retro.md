# Task Retro: Task 1 — Sign in through a Custom Tab (replaces the WebView)

**Task spec**: `specs/tasks/183-fix-passkey-login/task-1-custom-tab-sign-in.md`
**Sessions analyzed**: 1 (`ae46319a-e281-42ed-8af1-fd84412c38a9`)
**Date**: 2026-08-30

## Session Stats

| Metric | Value |
|--------|-------|
| Sessions | 1 |
| User messages (iterations) | 4 |
| Errors encountered | 0 |
| Duration | 20 min (2026-08-29 22:29:50Z → 22:50:34Z) |
| Input tokens | 102 |
| Output tokens | 41,167 |
| Cache read tokens | 5,321,937 |
| Cache creation tokens | 118,749 |
| Questions asked (AskUserQuestion) | 0 |
| Subagent spawns (Task) | 0 |
| Tool calls | 47 (all Bash — session ran in auto mode) |
| Resulting commit | `a067b1f` — *fix: sign in through the browser so passkeys work* (22 files, +497/−248) |

## What Went Well

**One pass, no rework.** 47 tool calls, zero errors, zero files edited more than three times, no
clarifying questions needed. The spec carried enough verified detail — exact line numbers, confirmed
collaborator signatures, framework source citations — that implementation ran start to finish
without a round trip.

**The hardest prescription landed exactly.** The spec's most subtle instruction was to use a single
`LaunchedEffect(Unit)` collector rather than the `LaunchedEffect(uiState)` pattern used elsewhere in
this codebase, because `WindowRecomposer` pauses the frame clock on `ON_STOP` while the Custom Tab is
in front. That was implemented as written *and* the rationale was carried into a code comment at
`BrowserLoginScreen.kt`, so the next reader cannot "simplify" it back. Task 2 depends on this
property, so getting it right here mattered beyond this task.

**Conditionals in the spec were resolved by checking, not assuming.** Step 1 said "PR 209 may have
already added `kotlinx-coroutines-test` — check first." The session checked, found it had not landed,
and added both lines.

**Verification exceeded what the spec asked for, and that is what caught the one real problem.** The
spec named four automated checks; the session additionally ran `lint`, `npm run docs:build`, and
walked the CI pipeline as `ci.yml` executes it. `lint` — not on the spec's list — is what surfaced
the `ExtraTranslation` failure.

**Honest verification reporting.** The final summary tabulated manual steps 5–11 as done / partial /
open rather than implying the task was verified, and explicitly flagged that `assembleFullRelease`
could not run (no keystore in this checkout) with `compileFullReleaseKotlin` substituted as the check
that actually exercises the `@Preview` composables.

**Correct task-boundary discipline.** When the user reported the Custom Tab not dismissing after
sign-in, the response checked the task-2 doc before answering and identified it as the documented
intermediate behaviour — rather than pulling task 2's `startActivity` into task 1's commit to make
the symptom go away. It also pre-flagged that the user's API 35 emulator is exactly the environment
where task 2's background activity start may be blocked.

**Working-tree hygiene.** The pre-existing `fastlane/Fastfile` modification was left unstaged and
called out, instead of being swept into the commit.

## Spec Accuracy

| Deliverable | Status | Notes |
|-------------|--------|-------|
| 1. `app/build.gradle` — `androidx.browser:browser:1.10.0` | as-specified | Added next to the other `androidx.*` entries. |
| 1. `app/build.gradle` — `kotlinx-coroutines-test:1.10.2` | as-specified | Spec's "check first, PR 209 may add it" branch resolved by checking; PR 209 had not landed, so the line is new. |
| 2. `core/util/UriOpenInCustomTabExtension.kt` | as-specified | Matches the spec snippet. No `resolveActivity` pre-check; `ActivityNotFoundException` caught. KDoc added recording *why* there is no pre-check. |
| 3. `WebViewScreenState.kt` → `BrowserLoginScreenState.kt` | as-specified | Git-tracked rename. `pollLoginServerIsActive` dropped, `browserLaunched` added with default `false`. |
| 4. `BrowserLoginViewModel.kt` | as-specified | All four changes from the spec's table present. `observeAuthorizationStatus()` and `pollLoginServer()` copied across unchanged; guard is `is Loaded`. No `SavedStateHandle` persistence — correctly deferred to task 3. |
| 5. `BrowserLoginScreen.kt` | as-specified | `url: String` only; `allowSelfSignedCertificates` dropped. Single `LaunchedEffect(Unit)` collector in the prescribed shape. `BrowserLoginLayout` follows `StartLayout` structure with `verticalScroll`, `dimensionResource`, `@Preview`. |
| 6. `StartScreenState.kt` + `StartScreen.kt` | as-specified | `WebView` → `SignIn`; navigates to `BrowserLoginScreenDestination(url)`; `onWebViewLoginClick` → `onSignInClick` at all four sites. `StartScreenViewModel` untouched, as specified. |
| 7. `values/strings.xml` | as-specified | Three strings added in alphabetical position; `error_webview_load_failed` removed. |
| 7. "Edit only `values/strings.xml`" | **modified** | The 8 `values-*/strings.xml` translations of `error_webview_load_failed` were also deleted. Leaving them orphaned fails CI: `lint` runs in the `build` lane, and `app/lint.xml` ignores `MissingTranslation` but **not** `ExtraTranslation`. See Learning 1. |
| 8. `ScreenshotsTestSuite.kt` rename | as-specified | Found by name, not line number, as instructed. `compileFullDebugAndroidTestKotlin` verified. |
| 8. Optional `browserLoginScreen()` case | dropped | Explicitly optional in the spec; correctly skipped (no store screenshot refresh in scope). |
| 9. Deletions (`webview/` × 2, `WebViewClient.kt`) | as-specified | All three gone; no dangling references to `WebViewScreenState`, `onWebViewLoginClick`, or `error_webview_load_failed`. |
| 10. `docs/faqs.md` FAQ entry | as-specified | Content as written, reflowed one-sentence-per-line to match the file's convention. `npm run docs:build` verified. |
| 11. `BrowserLoginViewModelUnitTest.kt` | as-specified | All seven cases with the exact names the spec prescribed. `StandardTestDispatcher` + `advanceTimeBy` over `POLL_DELAY`; ViewModel constructed inside each `runTest`. |
| `AndroidManifest.xml` unchanged | as-specified | Confirmed in the session's done-when check. |
| Commit format | as-specified | `fix: sign in through the browser so passkeys work` — subject only, no issue ref in the body. |

**14 of 15 deliverables landed exactly as specified. One diverged, and the divergence was correct.**

## Spec Gaps

Very few — this spec was unusually well-researched, and the gaps that existed were narrow.

**The Verification section did not mirror the CI lanes.** It enumerated four gradle checks
(`ktlintCheck`, `testFullDebugUnitTest`, `compileFullDebugAndroidTestKotlin`, `assembleFullRelease`)
but omitted `lint` and `npm run docs:build`, both of which CI actually runs. An implementer who
followed the spec's list literally would have pushed a red build. The session ran them anyway and
caught it — but that was diligence covering for the spec, not the spec doing its job.

**`assembleFullRelease` was not runnable as specified.** It fails at `validateSigningFullRelease`
without a `keystore.properties`, which is not present in a normal local checkout (CI creates an empty
one). The spec offered no fallback; the session substituted `compileFullReleaseKotlin`, which is what
actually exercises the `@Preview` composables. This is already captured in project memory
(*Release build needs Cryptomator keystore*), so the gap is in the spec template, not in what was
known.

## Over-Design

**None identified.** The spec was extremely prescriptive — 11 numbered sections with literal code
snippets, exact line numbers, and citations into Compose framework internals — and that density paid
for itself: one pass, no rework, and the one instruction most likely to be "simplified" wrong was
implemented correctly.

Worth stating plainly, because the obvious reading of the strings failure is "the spec was too
prescriptive": the opposite is true. The single place the spec got it wrong is the single place it
reasoned from a general principle ("translations are Weblate-managed") *without* checking the local
config. Prescription was not the problem; unverified prescription was.

## Under-Design

**The interaction between string removal and the lint configuration was never analysed.** The spec
specified removing `error_webview_load_failed` and separately specified not touching `values-*`, but
never asked what happens to the 8 existing translations of a string that no longer has a default.
Both halves were stated; their interaction was not reasoned about. This is the one place the spec
told the implementer to do something that could not work.

## Code Review & Corrections

### User Message Classification

| # | Timestamp | Intent | Summary |
|---|-----------|--------|---------|
| 1 | 2026-08-29 22:30:21Z | initial-prompt | "implement specs/tasks/183-fix-passkey-login/task-1-custom-tab-sign-in.md" |
| 2 | 2026-08-29 22:46:39Z | review-feedback | Device test on API 35 emulator, valid SSL cert: tab did not dismiss after sign-in; had to close it manually, then the Home redirect worked. |
| 3 | 2026-08-29 22:49:01Z | clarification | Confirms verification step 9 — dismissed the tab mid-flow and recovered it via the "Open browser" button. |
| 4 | 2026-08-29 22:49:56Z | approval | "lets commit this. i'll do the checks later." |

### Review Findings

| # | Finding | Root Cause | Scope | Domain |
|---|---------|-----------|-------|--------|
| 1 | Removing `error_webview_load_failed` from `values/` only leaves 8 orphaned translations; `ExtraTranslation` is an error in `app/lint.xml`, so CI's `build` lane fails. **Self-caught** by running `lint`, which the spec did not list. | spec-wrong | project | process |
| 2 | Custom Tab does not dismiss after successful sign-in (user, message 2). **Not a defect** — documented task-1 intermediate behaviour, resolved by pointing at the task-2 doc. No code change. | n/a — expected behaviour | — | — |

No user-originated code-review findings. The only substantive correction in the session was
self-identified before the user saw the code.

## Emerged Designs

**Run the CI pipeline, not the spec's checklist.** The spec's four-item verification list was treated
as a floor rather than a definition of done, and the extra checks are what caught the only real
problem. This should become the standing instruction in the task-spec template: *Verification must
enumerate every lane CI runs, and the implementer runs the pipeline regardless.*

**Record framework rationale at the call site, not only in the spec.** The `LaunchedEffect(Unit)`
comment in `BrowserLoginScreen.kt` and the "no `resolveActivity` pre-check" KDoc in
`UriOpenInCustomTabExtension.kt` both preserve reasoning that is invisible from the code and would
otherwise be refactored away once the spec is archived. Both were emergent — the spec explained the
reasoning to the implementer but never asked for it to be left in the source.

**`compileFullReleaseKotlin` as the keystore-free stand-in for `assembleFullRelease`** when the only
goal is proving `@Preview` composables compile.

**Collapsing `Initial` and `Authenticated` into one `when` branch** — the spec's state table listed
them as separate rows that both render `Loader`; the implementation merged them. Cosmetic, and
correct.

## Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | The spec instructed leaving `values-*/strings.xml` untouched and letting Weblate reconcile a removed string, but `app/lint.xml` treats `ExtraTranslation` as an error (only `MissingTranslation` is ignored), so orphaned translations fail CI's `build` lane. Removing a default string requires removing its translations in the same commit. | project | tech-stack:android | spec-wrong |
| 2 | The spec's Verification section listed four gradle checks but omitted `lint` and `npm run docs:build`, both of which CI runs. A spec's verification list must mirror the CI lanes, or following it literally ships a red build. | team | process | under-design |
| 3 | The task deliberately shipped a visibly incomplete behaviour (Custom Tab stays on top after success — task 2's job), and the spec documented this clearly, but the implementation handoff summary did not restate it. The user device-tested, hit it, and had to ask whether it was a bug. When a task knowingly ships an incomplete user-visible behaviour, the handoff must lead with what will look broken and why. | team | process | baseline-miss |
| 4 | `assembleFullRelease` cannot run in a normal local checkout (`validateSigningFullRelease` needs a `keystore.properties` that only CI creates). Specs that want `@Preview` compilation verified should ask for `compileFullReleaseKotlin` instead. | project | tech-stack:android | spec-wrong |
