# Spec Retro: Fix — copying a single ingredient copies the default amount

**Spec**: `specs/spec/208-fix-copy-single-ingredient-copies-default-amount.md`
**Tasks**: 2 (0 corrections on the code; 1 non-code correction)
**Date**: 2026-08-30

## Summary

The spec performed near the top of its class on *design* and consistently poorly on *verification
commands*. Every architecture decision held up under implementation — including the two the spec
itself flagged as risky (the `DisposableEffect` timing trace and the deliberate two-layer fix) — and
both tasks landed from a single `implement <path>` prompt with zero code corrections and zero review
feedback. The failures were all in the last section of each task file: two consecutive verification
blocks prescribed a Gradle command that cannot run in this project, and the "required" manual
verification was assigned to an implementer structurally incapable of performing it. The net
regression posture is worth stating plainly: six green unit tests ship, and none of them would go red
if the actual fix were reverted.

| Metric | Value |
|--------|-------|
| Tasks | 2 |
| Total corrections | 1 (message 4 of task 2 — "are you doing anything?", about agent liveness, not the change) |
| Tasks with 0 corrections | 2 (0 code corrections in both) |
| Learnings by root cause | spec-gap: 1, spec-wrong: 2, over-design: 0, under-design: 2, baseline-miss: 3, convention-miss: 2 |

Branch diff: 9 files, +1067/−14 — of which 839 lines are the spec and task markdown itself, and
+228/−14 is code (`RecipeDetailScreen.kt` 60 changed lines, `RecipeDetailViewModel.kt` +10,
`RecipeDetailViewModelUnitTest.kt` +170, `app/build.gradle` +1, `ScreenshotsTestSuite.kt` +1).

## Architecture Decisions Scorecard

| # | Decision | Score | Notes |
|---|----------|-------|-------|
| 2.1a | Resolve the copy text through the ViewModel, by index (`getIngredientAt`) | held-up | Implemented verbatim (`RecipeDetailViewModel.kt:145`); `ktlintFormat` only re-wrapped the safe-call chain. The index-stability claim (`recalculateIngredients` is a plain `map`) was never contradicted. The spec was honest that this layer does not fix the bug — task 2's retro confirms the cost is visible: threading one parameter through two composables, two previews and an androidTest file is most of task 2's +86/−14. Accepted, not a defect. |
| 2.1b | Remove the stale-capture hazard at its source (`BindLongClick` = `DisposableEffect` + `rememberUpdatedState`) | held-up | Shipped verbatim, including the two-line listener body written to dodge ktlint. The spec called the effect-timing swap "the one way the change could silently break the feature" and traced `Composition.applyChangesInLocked` on paper; task 2 confirmed it on an emulator — the listener fired on the first long-press with no prior recomposition. The paper trace was right. |
| 2.2 | Clipboard write stays in the composable | held-up | No deviation, no discussion. `scope`, `clipboard` and `context` stayed in `Ingredients` exactly as §3.3 and the task file (which corrected the spec's own loose prose about `context` moving) required. |
| 2.3 | Empty / out-of-range fallback returns `""`, handler skips the write | held-up | Implemented as written; the gesture is still consumed on a blank result. Note the `data.ingredients` fallback branch is still untested — case 4 covers out-of-range, nothing covers "calculated empty, data present". The spec called it defensive-only, which is why this is a note and not a finding. |
| 2.4 | Whole-recipe copy is already correct — add a guard, no production change | held-up | The finding was right: nothing in the share path needed changing, and cases 5–6 lock it in. This decision is what created task 1, so it carries the cost noted below — the first task of a bugfix shipped a guard for working code and zero coverage of the reported bug. Defensible (it stands up the harness task 2 needs) and correctly reasoned, but worth seeing as a decision with a schedule cost, not a free extra. |
| 2.5 | Add `kotlinx-coroutines-test:1.10.2` | held-up | One line, no friction — despite the spec explicitly predicting "first-run friction on the new dependency". A wrong prediction that cost nothing. The harder pre-verified claims underneath it (`SavedStateHandle(Map)` never touches `Bundle`; stubbing `preferencesFlow` on a final class already works here) were both correct and each saved a round trip. |

No decision scored partially-wrong or wrong. That is unusual and is the spec's strongest result.

## Task Breakdown Assessment

| Task | Corrections | Messages | Assessment |
|------|-------------|----------|------------|
| Task 1: ViewModel test harness + share-path regression guard | 0 | 1 | well-sized |
| Task 2: Fix stale ingredient copy, end to end | 1 (non-code) | 5 (4 logged + 1 queued) | well-sized |

**Right-sizing.** By the primary signal — corrections — both tasks are clean. Task 1 was one prompt
to commit in ~5 minutes; task 2 was one prompt to commit in ~8 minutes. The only `correction`-intent
message in the entire session (task 2, message 4) was the user asking whether anything was happening
while an `adb` boot-wait loop hung, which is an implementer defect, not a scoping defect. Task 2's
other three messages were an out-of-band instruction ("please also commit the specs"), an unrelated
environment report, and the shipping approval.

**Dependencies.** The declared dependency (task 2 needs task 1's test file, fixture and coroutines
dependency) was real and correctly ordered. The spec went further and pre-numbered the test cases
5–6 in task 1 and 1–4 in task 2 specifically so task 2 could append without renaming — task 2 did
exactly that, +29 lines to the existing file, no renumbering, no second test file. That is
dependency management done at the right granularity and it is the breakdown's best moment.

No hidden dependencies surfaced. Nothing in task 2 required work from a later task.

**Merge/split.** Neither. A merge is arguable — task 1 is small, touches no production code, and both
tasks were executed by the same session hours apart — but the split earns its keep: release notes are
derived from commit messages here, so splitting produced a clean `test:` commit and a clean `fix:`
commit rather than one `fix:` commit carrying a test harness. Keep the shape.

**Missing tasks.** Two things had to be done and were in nobody's task:

1. **Committing the spec and task files.** The user had to ask for it mid-task ("please also commit
   the specs", 21:23:50Z), and it landed as `64fe11e`, +839 lines of markdown, after the fix commit.
   The breakdown README never says the spec artifacts get committed, on which branch, or in which
   order relative to the code.
2. **Branch, push, PR.** Both task files have a `## Commit` section and neither names a branch. The
   session was on `main` both times and branched immediately before committing. It worked twice, so
   no damage — but it worked because the implementer supplied the missing step, not because the
   breakdown covered it.

## Spec Review

| # | What the spec said | What happened | What the spec should have said |
|---|-------------------|---------------|-------------------------------|
| 1 | Task 1 Verification: `` `./gradlew testDebugUnitTest --tests '*RecipeDetailViewModelUnitTest*'` `` | Gradle rejected it: *"Candidates are: 'testFullDebugUnitTest', 'testGooglePlayDebugUnitTest'…"*. The task does not exist in a flavored project. One wasted round trip; re-run as `testFullDebugUnitTest`. The wrong command is inherited from `CLAUDE.md:19`, so every future spec will copy it again. | `./gradlew testFullDebugUnitTest --tests '*RecipeDetailViewModelUnitTest*'` — and fix the source: `CLAUDE.md:19` currently reads ``- `./gradlew testDebugUnitTest` - Run debug unit tests`` and should read ``- `./gradlew testFullDebugUnitTest` / `./gradlew testGooglePlayDebugUnitTest` - Run debug unit tests for one flavor. The project is flavored (`full` / `googlePlay`), so there is no `testDebugUnitTest` task.`` |
| 2 | Task 2 Verification: `` `./gradlew assembleFullRelease   # compile-checks the @Preview updates, as CI does` ``, repeated in Done-when as *"`./gradlew ktlintCheck test compileFullDebugAndroidTestKotlin assembleFullRelease` all succeed."* | The task aborts at `validateSigningFullRelease` — before compiling anything — because it needs the signing keystore. Substituted with `compileFullReleaseKotlin` + `assembleFullDebug` and disclosed ("The release assemble itself is unverified"). Second consecutive task whose verification block prescribed a command that cannot run as written. | `./gradlew compileFullReleaseKotlin   # compile-checks the @Preview updates` — the narrowest task that delivers the stated check. `assembleFullRelease` additionally packages and signs, which proves nothing more about the code and adds a signing dependency. Rule for the template: prescribe the smallest Gradle task that proves the claim in the comment next to it. (Keep the machine-specific reason out of the file — committed specs must not document one checkout's setup.) |
| 3 | Task 1 Verification: *"Both new tests must pass with the assertions above **actually evaluated** — if `getShareText()` returns early (`_state.value.data` null), `verify(recipeFormatter).format(...)` fails … Do not 'fix' that by relaxing the assertion; fix the fixture."* | The risk was named precisely but no technique was given for confirming a pass was not vacuous. Claude invented one: temporarily rewrite `assertEquals(5, captor.firstValue.yield)` to `assertEquals(999, …)`, confirm red, restore and `grep`-verify the restore — all in one Bash invocation so the tree could never be left mutated. The guess was right; the spec could have asked. | Add the technique, not just the warning: *"Prove each new assertion is evaluated before calling the task done: in a single command, change one expected value to a deliberately wrong one, run the test, confirm it fails with the expected comparison, restore the original, and `grep` to verify the restore. Never leave the mutation in the working tree across commands."* |
| 4 | Task 2: *"### Manual verification (required — instrumented tests do not run in CI)"* with steps 1–6, each needing a recipe on a logged-in server; plus *"do not add an instrumented test to compensate **unless you also arrange for it to run**."* Done-when repeats it: *"Long-pressing an ingredient copies the currently displayed text (manual steps 1–4 pass)."* | The agent cannot log into a Nextcloud server, so a load-bearing Done-when criterion was unreachable, and the one sanctioned escape hatch carried a condition ("arrange for it to run") with no operational meaning. Claude invented the policy mid-task: boot an API 29 emulator (deliberately, to also hit the `SDK_INT <= S_V2` toast branch), write a throwaway `TmpLongPressCopyTest.kt`, run green, revert the fix and re-run to get `expected:<[5]00 g flour> but was:<[4]00 g flour>` — #208 reproduced verbatim — restore, delete. The user's own device confirmation arrived an hour later. | Name the owner and define the substitute: *"Steps 1–6 are the **maintainer's**; the implementer must report them as pending rather than claim them. The implementer's substitute is a throwaway instrumented test: write it, run it on an emulator, **prove it goes red against the un-fixed code**, then delete it — do not commit a test CI will never execute. 'Arrange for it to run' means exactly that red-then-green demonstration."* |
| 5 | Task 2: *"**Positional-args note:** the `Ingredients(...)` call at `:407` passes its arguments **positionally** … Either add the new argument in the correct position **or** convert that call to named arguments — do not assume you can append it."* | The warning itself was load-bearing and paid for itself — a blind append would have compiled and silently mis-bound `isShowIngredientSyntaxIndicator`. But the *choice* was handed to the implementer. All eight arguments were converted to named form, which is the better call and also accounts for most of the churn in that hunk (an 8-line call became 13 lines) in a diff a reviewer has to read. | Decide it: *"Convert the whole call to named arguments — all eight. It is the only positional call site in the file, and a positional insert is a silent mis-binding hazard; the extra diff lines are worth it. Do not append the argument positionally."* A spec that enumerates line numbers and per-import deltas should not leave a reviewer-visible diff-size decision open. |

Not tabled, because the spec already weighed it and the retros record it as an accepted cost rather
than a defect: the fix is two layers (§2.1a + §2.1b) where one would close #208. Spec §5 states the
tradeoff, names which layer a reviewer should drop for a smaller diff, and chooses testability. Task
2's retro confirms the rationale held. Recorded here so it is not re-litigated.

## Undocumented Conventions

| # | Convention | Evidence | Where to document |
|---|-----------|----------|-------------------|
| 1 | Commits must carry **no** attribution trailer — no `Co-Authored-By`, no other. | Task 1 Learning 1, Task 2 Learning 1. All three commits on the branch (`893fdda`, `c815f10`, `64fe11e`) carry `Co-Authored-By: Claude …`. The user did not catch it in-session, so it shipped. | **Already documented** — `~/.claude/CLAUDE.md`: *"Never add a `Co-Authored-By` trailer (or any other attribution trailer)"*. Documentation is therefore not the fix: the harness system prompt prescribes the trailer and beat the user instruction three times in one session. This needs enforcement where commits are composed (a `commit-msg` hook or a `PreToolUse` hook on `git commit` that strips trailers), not another line of prose. Hand to meta-retro as an enforcement item. |
| 2 | *Cross-reference, not a convention-miss:* the project is flavored, so `testDebugUnitTest` does not exist. | Task 1 Learning 2 (root cause `spec-wrong`, so it is reviewed in row 1 above) — but the defect's home is a docs file, which makes it meta-retro material. | `CLAUDE.md:19`, Testing section. It documents a command that cannot run, and specs copy from it. |

No other `convention-miss` learnings were filed.

## What Went Well

**Two tasks, one prompt each, zero code corrections.** `implement <task spec path>` was the entire
user input for both implementations. No clarification, no review feedback, no scope negotiation. The
spec was precise enough to be executed rather than interpreted.

**The pedantic passages were the ones that paid.** Each of these prevented a specific, concrete
failure that would otherwise have cost a round trip or shipped a silent defect:

- The `UnconfinedTestDispatcher` mandate *with its reason* (`increaseYield()` counts up from current
  state, so acting before `init` settles yields 1 instead of 5). The implementation copied the
  rationale into a KDoc rather than merely obeying it.
- The `NumberFormat` grouping caveat (`1200 g` → `1,200 g`), also carried into the code as a fixture
  KDoc, so the next person extending it inherits the trap.
- The positional-args warning — a blind append would have compiled and mis-bound a boolean.
- The `ScreenshotsTestSuite.kt` warning that CI never compiles androidTest, which is why
  `compileFullDebugAndroidTestKotlin` was run and the `screenshots` lane will not break weeks later
  behind a green CI history.
- The line-by-line import audit: `android.widget.TextView` removed, `rememberUpdatedState` added,
  `DisposableEffect` / `getValue` / `View` / `LaunchedEffect` correctly left alone.

**Pre-verified claims held under execution.** Everything the spec asserted from reading dependency
sources or runtime internals — twain builds its `TextView` in the `AndroidView` factory; Markwon
installs a `LinkMovementMethod` that rules out `combinedClickable`; `DisposableEffect` runs late
enough for `findViewById`; `SavedStateHandle(Map)` never touches `Bundle`; `.editorconfig` exempts
`@Composable` from ktlint's naming rule; indices are stable across recalculation — was correct. None
cost a round trip. The riskiest of them was confirmed on hardware.

**Test-case numbering across the task boundary.** Numbering task 1's cases 5–6 and task 2's 1–4,
explicitly so task 2 could append, is a small piece of foresight that worked exactly as intended.

**The spec's §5 Readiness was honest and accurate.** It named its own two open risks — the redundant
layer, and the fact that #208 would ship without an automated regression test — and both played out
exactly as described. A readiness section that correctly predicts its own weaknesses is more useful
than one that claims none.

**Two emerged techniques worth promoting into the template**, both discovered by the implementer and
both filling gaps this review has now tabled: the single-command mutation check on a fresh assertion
(row 3), and the throwaway discriminating instrumented test — **boot it, break it, prove the proof
fails, restore, discard** (row 4).

## Spec-Level Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | Nothing that ships in this change would fail if the fix were reverted. The six unit tests exercise `getIngredientAt` on the ViewModel; restoring the original `LaunchedEffect(Unit)` capture in `Ingredients` leaves all six green, and `ScreenshotsTestSuite` passes `getIngredientAt = { "" }`. The only artifact that ever discriminated — task 2's throwaway instrumented test — was deleted by design. Visible only across both tasks: task 1 guarded a path that was never broken, task 2 guarded the layer that was not the bug. The spec should either arrange for one instrumented test to actually run in CI, or state in the fix commit / PR that #208 has no automated regression guard so a future refactor of `BindLongClick` knows what it is not protected by. | project | testing | under-design |
| 2 | Two consecutive verification blocks prescribed a Gradle command that cannot run as written (`testDebugUnitTest`, then `assembleFullRelease`). Each was filed per-task, but the pattern is a spec-authoring habit: verification blocks are assembled from the `CLAUDE.md` and CI vocabulary and never executed against the actual task list. Every command in a verification block should be checked to exist (`./gradlew tasks`, or against a known-good list) before the spec ships, and each should be the narrowest task that proves the claim written next to it. | project | tech-stack:gradle | spec-wrong |
| 3 | Neither task file says which branch to commit on, though both have a `## Commit` section prescribing the subject line, the issue-reference policy, and what not to touch. The session was on `main` both times and branched itself immediately before committing. Once is baseline behaviour; twice across every task in the spec is a template omission. The task template's `## Commit` section should open with the branch (`work on <branch>, cut from main if it does not exist`). | team | process | spec-gap |
| 4 | The spec artifacts themselves are not in any task. The user had to interrupt task 2 with "please also commit the specs", and 839 lines of markdown landed as a separate commit after the fix. The breakdown README should state where the spec and task files are committed and when — most naturally as the first commit on the feature branch, before task 1 — so the implementer does not have to be told. | team | process | spec-gap |

## Follow-ups for meta-retro

- Fix `CLAUDE.md:19` (`testDebugUnitTest` → the two flavored tasks, with a note that the unflavored
  task does not exist). It is the upstream source of spec review row 1.
- Enforce the no-attribution-trailer rule mechanically; three violations in one session show that
  restating it in prose does not work.
- Promote the mutation check and the throwaway discriminating instrumented test into the task
  template as standard verification steps.
- Add branch-name and spec-commit lines to the task template.
