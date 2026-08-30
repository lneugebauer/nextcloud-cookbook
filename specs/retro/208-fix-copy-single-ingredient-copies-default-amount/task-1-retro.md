# Task Retro: ViewModel test harness + whole-recipe share regression guard

**Task spec**: `specs/tasks/208-fix-copy-single-ingredient-copies-default-amount/task-1-viewmodel-test-harness-share-guard.md`
**Sessions analyzed**: 1 (`9b2bd995`, branch `main`)
**Date**: 2026-08-30

## Session Stats

The session covered both task 1 and task 2. Numbers are scoped to the task 1 window
(2026-08-29 18:32:51Z, first prompt → 18:38:06Z, commit `893fdda`); session totals follow in
parentheses where they differ.

| Metric | Value |
|--------|-------|
| Sessions | 1 |
| User messages (iterations) | 1 (session: 5) |
| Errors encountered | 2 tool errors + 1 build failure + 1 intentional failing run (session: 3 tool errors) |
| Duration | ~5 min active (session: 219 min; 149 min idle between task 1 and task 2) |
| Input tokens | 120 (session: 184) |
| Output tokens | 34,374 (session: 64,683) |
| Cache read tokens | 4,335,327 (session: 10,821,190) |
| Cache creation tokens | 139,030 (session: 221,774) |
| Questions asked (AskUserQuestion) | 0 (session: 1, during task 2) |
| Subagent spawns (Task) | 0 |
| Tool calls | 31 Bash (session: 86 Bash, 1 AskUserQuestion) |

Error breakdown for task 1: a `SIGPIPE` exit from a `cat`-into-`head` probe; a zsh no-match failure
on an unquoted `--include=*.kt` grep flag; a `kapt` build failure from a local filesystem
filename-length limit (environment, not the change). The fourth non-zero run was a *deliberate*
mutation of an assertion — see Emerged Designs. `extract-signals.sh` counts none of the last two as
errors, because both were captured through `2>&1 | tail` and exited 0.

## What Went Well

**One prompt, no corrections.** The user's only task-1 message was `implement <task spec path>`.
There was no clarification, no correction, no review feedback, and no question back to the user. The
commit landed ~5 minutes later.

**The spec's precision paid for itself directly.** Three of its more laborious passages each
prevented a specific failure that would otherwise have cost a round trip:

- The `UnconfinedTestDispatcher` mandate, with the reason spelled out (`increaseYield()` counts up
  from current state, so acting before `init` settles yields 1 instead of 5). The implementation
  copied the rationale into a file-level KDoc rather than just obeying it.
- The `NumberFormat` grouping caveat (`1200 g` → `1,200 g`). Also carried into the code as a KDoc on
  the fixture, so the next person extending it inherits the trap.
- Naming cases 5 and 6 rather than 1 and 2, explicitly so task 2 could append 1–4 without renaming.
  Task 2 did exactly that (`c815f10`, +29 lines to the same file) and no test was renumbered.

**Pre-verified fixture claims held up under execution.** The spec asserted that
`SavedStateHandle(Map)` never touches `Bundle` and that stubbing `preferencesFlow` on a final class
already works in this project, each with a cited precedent. Both were correct — neither needed
`returnDefaultValues` nor any Mockito configuration change.

**The environment failure was diagnosed, not worked around blindly.** The `kapt` failure was
correctly identified as pre-existing and unrelated to a test-only change, resolved without
interrupting the user, *and* written to durable agent memory in the same turn — so the next session
does not re-derive it.

**Scope discipline.** `git show --stat 893fdda` is 2 files, +142/−0, nothing under `app/src/main/`,
exactly as the spec's Done-when required.

## Spec Accuracy

| Deliverable | Status | Notes |
|-------------|--------|-------|
| `testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2'` in `app/build.gradle` | as-specified | Single line, placed with the other `testImplementation` entries |
| New `RecipeDetailViewModelUnitTest.kt` at the prescribed path | as-specified | 141 lines, new file |
| `SyncRecipesUseCaseUnitTest` style: plain `mock()` fields in `@Before`, not `@Mock` + `openMocks` | as-specified | |
| `preferencesManager` stub with the full `Preferences(...)` construction | as-specified | All four required args copied from the cited precedent |
| `recipeFormatter` as a bare `mock()` of the interface | as-specified | No Android `Resources` needed, as predicted |
| `recipeRepository` stubs installed before ViewModel construction | as-specified | Both flows stubbed ahead of the constructor call |
| `SavedStateHandle(mapOf("recipeId" to "1"))` as a real instance | as-specified | |
| Real `YieldCalculatorImpl(Locale("en"))` | as-specified | |
| `RECIPE` fixture (yield 4; `400 g flour`, `2 eggs`, `salt`) | modified | Values verbatim, but placed in a `private val` inside a `companion object` rather than the spec's top-level `val`. Better — keeps it off the package namespace — and ktlint-clean. |
| `Dispatchers.setMain(UnconfinedTestDispatcher())` in `@Before`, `resetMain()` in `@After` | as-specified | Plus a KDoc reproducing the spec's reasoning |
| Case 5 `getShareText_afterIncreaseYield_...` | as-specified | `argumentCaptor<Recipe>()`, asserts yield 5 and the three recalculated strings |
| Case 6 `getShareText_beforeYieldChange_...` | as-specified | Asserts yield 4 and the three original strings |
| No file under `app/src/main/` modified | as-specified | Confirmed by the commit stat |
| Verification: `./gradlew testDebugUnitTest --tests '*RecipeDetailViewModelUnitTest*'` | modified | The task is ambiguous under this project's two flavors — Gradle rejected it with *"Candidates are: 'testFullDebugUnitTest', 'testGooglePlayDebugUnitTest'…"*. Re-run as `testFullDebugUnitTest`. See Learning 2. |
| Verification: `./gradlew ktlintFormat` / `ktlintCheck` / `test` | as-specified | All run, all green — though every Gradle invocation needed a relocated build directory on this machine |
| Conventional commit, `test:` subject, no issue ref in the body | modified | Subject is exactly the spec's suggestion. The body carries a `Co-Authored-By` attribution trailer that the user's global instructions forbid outright. See Learning 1. |
| Feature branch | added | Not mentioned anywhere in the task spec. The session was on `main`; a branch was created immediately before committing. |
| Mutation check on the assertions | added | See Emerged Designs |
| Gradle build-directory relocation | added | Environment workaround; also recorded in agent memory, correctly kept out of the repo |

Nothing in the task spec was dropped.

## Spec Gaps

**Almost none — and that is the headline finding.** The one omission worth naming is that the task
spec never says which branch to commit on, even though it has a `## Commit` section prescribing the
subject line, the issue-reference policy, and what *not* to touch. Claude branched off `main` on its
own, so nothing broke; per the deduplication rule this is baseline practice rather than a spec
defect, and it is logged here only as context for why no learning is filed against it.

The spec's own Verification section pre-announced *"expect first-run friction on the new
`kotlinx-coroutines-test` dependency (Gradle sync, import resolution). That is normal — resolve it
rather than working around it."* In the event there was no dependency friction at all; the friction
came from somewhere the spec could not have predicted (below).

## Over-Design

None attributable to task 1. The spec's own §5 flags the *feature* as arguably two-layered
(`BindLongClick` plus the ViewModel lookup), but both of those land in task 2. Task 1's deliverable —
a test harness plus two guard cases — has no redundant part.

One judgment call worth recording rather than criticising: task 1 exists to guard a path the spec
had already proven **correct** (§2.4 — the share text was never affected by #208). Spending the
first task of a bugfix on a non-regression guard for working code is defensible, because it is what
stands the ViewModel test harness up for task 2's four real cases. But it does mean the task shipped
zero coverage of the reported bug, and the retro should not read the clean run as evidence that #208
is covered — §5 of the spec is explicit that the Compose binding which actually failed still has no
automated test.

## Under-Design

The spec said the assertions must be *"actually evaluated"* and named the failure mode — if
`getShareText()` returns early on null state, `verify(...).format(...)` fails — plus the instruction
*"do not 'fix' that by relaxing the assertion; fix the fixture."* It identified the risk precisely
but prescribed no technique for confirming a pass was not vacuous. Claude filled the gap well (see
Emerged Designs), so the guess was right; the spec could simply have asked for it.

## Code Review & Corrections

### User Message Classification

Only message 1 belongs to task 1. Messages 2–5 are listed for context and belong to task 2 and to
post-implementation work.

| # | Timestamp | Intent | Summary |
|---|-----------|--------|---------|
| 1 | 2026-08-29T18:32:51Z | initial-prompt | `implement specs/tasks/208-.../task-1-viewmodel-test-harness-share-guard.md` |
| 2 | 2026-08-29T21:02:04Z | initial-prompt (task 2) | `implement task-2-fix-stale-ingredient-copy.md` |
| 3 | 2026-08-29T21:23:50Z | instruction (task 2) | `please also commit the specs` |
| 4 | 2026-08-29T21:30:30Z | clarification (task 2) | Reports `:app:kaptFullDebugKotlin` failing when launching from Android Studio |
| 5 | 2026-08-29T22:11:20Z | approval (task 2) | Confirms the fix works on a real device; asks to push and open a PR |

Zero corrections and zero review feedback across the whole session — for task 1, zero messages of
any kind after the initial prompt.

### Review Findings

| # | Finding | Root Cause | Scope | Domain |
|---|---------|-----------|-------|--------|
| 1 | Commit `893fdda` carries a `Co-Authored-By` attribution trailer | convention-miss | team | process |
| 2 | `./gradlew testDebugUnitTest` is ambiguous under the `full`/`googlePlay` flavors | spec-wrong | project | tech-stack:gradle |
| 3 | Unquoted `--include=*.kt` aborted a `grep` under zsh | baseline-miss | team | process |

## Emerged Designs

**Mutation-checking a fresh assertion.** Rather than trusting a green run, the session temporarily
rewrote `assertEquals(5, captor.firstValue.yield)` to `assertEquals(999, …)`, confirmed the test
then failed, restored the original in the same command, and `grep`-verified the restore — all in one
Bash invocation, so the working tree was never left mutated even if the run had been interrupted.
This is the right general answer to *"prove the assertion is actually evaluated"* and is cheaper than
reasoning about whether the fixture settled. Worth promoting into the spec template as a standard
verification step for any newly written test, and worth keeping the one-command
apply-run-revert-verify shape.

**Relocating the Gradle build directory via an init script** to sidestep a local filesystem
filename-length limit that `kapt`'s Hilt-generated output exceeds. Environment-specific, correctly
kept out of the committed spec and written to agent memory instead. Note the limitation discovered
later in the session: this fixes command-line runs only, and the IDE Run button keeps failing — which
is what produced user message 4 nearly three hours later.

## Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | The commit was written with a `Co-Authored-By: Claude …` trailer even though the user's global instructions say *"Never add a `Co-Authored-By` trailer (or any other attribution trailer)"*. The harness system prompt prescribes the opposite, and the system prompt won — on all three commits of the branch. The user did not catch it in-session, so it shipped. Project instructions must beat the default harness template on commit formatting. | team | process | convention-miss |
| 2 | The spec's verification block prescribed `./gradlew testDebugUnitTest`, which does not exist in a flavored Android project — Gradle rejected it as ambiguous between `testFullDebugUnitTest` and `testGooglePlayDebugUnitTest`. The wrong command is inherited from `CLAUDE.md`'s Testing section, so every future spec will copy it again until that file is corrected. | project | tech-stack:gradle | spec-wrong |
| 3 | A `grep --include=*.kt` invocation was written unquoted and zsh aborted it with `no matches found` before `grep` ever ran. Glob-bearing flags need quoting in zsh; this recurs across sessions and each occurrence costs a wasted round trip. | team | process | baseline-miss |
| 4 | The spec demanded that the new assertions be *"actually evaluated"* and named the vacuous-pass failure mode, but left the technique unspecified. The gap was filled correctly and inventively (mutation check), which suggests promoting that technique into the spec template rather than restating the warning. | team | process | under-design |
