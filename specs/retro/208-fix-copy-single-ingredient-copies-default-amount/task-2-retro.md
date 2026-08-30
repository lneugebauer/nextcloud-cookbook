# Task Retro: Fix stale ingredient copy, end to end

**Task spec**: `specs/tasks/208-fix-copy-single-ingredient-copies-default-amount/task-2-fix-stale-ingredient-copy.md`
**Sessions analyzed**: 1 (`9b2bd995`, branch `main`)
**Date**: 2026-08-30

## Session Stats

The session covered both task 1 and task 2. Numbers are scoped to the task 2 window
(2026-08-29 21:02:04Z, first prompt → 22:12:20Z, PR opened); the implementation sub-window ran
21:02:04Z → 21:10:13Z (commit `c815f10`). Session totals follow in parentheses where they differ.

| Metric | Value |
|--------|-------|
| Sessions | 1 |
| User messages (iterations) | 5 — 4 logged + 1 queued (session: 6; `extract-messages.sh` sees 5) |
| Errors encountered | 1 tool error + 1 real build failure + 1 intentional red-phase failure (session: 3 tool errors) |
| Duration | ~8 min to commit; 70 min including the environment detour and the PR (session: 219 min) |
| Input tokens | 120 (session: 184) |
| Output tokens | 47,495 (session: 64,683) |
| Cache read tokens | 8,502,311 (session: 10,821,190) |
| Cache creation tokens | 151,081 (session: 221,774) |
| Questions asked (AskUserQuestion) | 1 — about the environment, not the task |
| Subagent spawns (Task) | 0 |
| Tool calls | 55 Bash + 1 AskUserQuestion (session: 86 Bash, 1 AskUserQuestion) |

Token figures are deduplicated per `requestId`, matching `extract-signals.sh`; the task 1 and task 2
windows sum exactly to the session total.

The one flagged tool error is `exit 143 / Command timed out after 10m 0s / error: more than one
device/emulator` — a boot-wait loop that could never terminate with two emulators attached (Learning
5). The real build failure is `assembleFullRelease` aborting on `validateSigningFullRelease`
(Learning 2). The third non-zero run was *deliberate*: the fix was temporarily reverted to prove the
throwaway instrumented test discriminates (see Emerged Designs). No ktlint violation occurred at any
point.

## What Went Well

**Zero corrections on the implementation itself.** The user's only task-2 instruction was
`implement task-2-fix-stale-ingredient-copy.md`. No clarification was needed, no review feedback was
given, and the commit landed 8 minutes later. Every later message was about the environment, the
specs commit, or shipping — none was about the change.

**The spec was followed line for line, including the parts that were easy to skip.** Three of its
more pedantic passages each prevented a concrete failure:

- The **positional-args warning** on the `Ingredients(...)` call at `:407`. The call really was
  positional; appending an eighth argument blindly would have compiled and silently mis-bound
  `isShowIngredientSyntaxIndicator`. Claude converted the call to named arguments instead.
- The **`ScreenshotsTestSuite.kt` warning** that CI never compiles androidTest. The file was updated
  *and* verified with `compileFullDebugAndroidTestKotlin`, so the `screenshots` lane will not break
  weeks later with a green CI history behind it.
- The **import audit**. `android.widget.TextView` was removed (its only use was the replaced line)
  and `rememberUpdatedState` added; `DisposableEffect`, `getValue`, `View`, and `LaunchedEffect`
  were correctly left alone. `LaunchedEffect(state)` at `:149` survives, exactly as specified.

**The `DisposableEffect` timing risk the spec flagged as "the one way this could silently break" was
actually retired.** The spec traced `Composition.applyChangesInLocked` on paper. Claude confirmed it
on hardware: the emulator run showed the listener firing on the first long-press with no prior
recomposition, so `findViewById` does resolve the interop view at `DisposableEffect` time.

**The change was proven to fix #208, not just proven to compile.** See Emerged Designs — the
throwaway instrumented test reproduced the bug verbatim (`expected:<[5]00 g flour> but was:<[4]00 g
flour>`) against the old code.

**Substitutions were disclosed, not hidden.** Both deviations — `assembleFullRelease` and the manual
steps — were reported in the summary with the reason, including the flat statement "The release
assemble itself is unverified."

**The environment detour was handled as real engineering.** The kapt failure was measured (touching
filenames of increasing length to find the 143-byte eCryptfs cap empirically), the library was
checked for an escape hatch (`dagger-compiler-2.57.2.jar` unzipped; only three options exist, none
relevant), the offender was confirmed to be a lone outlier (151 chars vs 131 for the next-longest),
and its introducing commit was dated to explain when the user's IDE builds started failing. The
resulting `AskUserQuestion` offered three options with the real tradeoff of each spelled out. That
is the right shape for an irreversible-ish change to the user's machine.

**Scope discipline.** `git show --stat c815f10` is 4 files, +86/−14 — precisely the files the spec
names, and nothing else. The user's unrelated `fastlane/Fastfile` edit was left untouched throughout.

## Spec Accuracy

| Deliverable | Status | Notes |
|-------------|--------|-------|
| `getIngredientAt(index)` on `RecipeDetailViewModel`, beside `getShareText()` | as-specified | Semantics identical; `ktlintFormat` re-wrapped the safe-call chain across 8 lines instead of the spec's 3 |
| `BindLongClick` private composable, placed near `KeepScreenOn` | as-specified | At `:236`, immediately after `KeepScreenOn` |
| Listener body written across two lines, not `{ ...; true }` | as-specified | `ktlintCheck` green on first run |
| `DisposableEffect(viewId)` + `rememberUpdatedState` + `onDispose` clearing the listener | as-specified | Verbatim |
| `RecipeDetailLayout` signature gains `getIngredientAt` | as-specified | `:372` |
| `RecipeDetailLayout` call passes `viewModel::getIngredientAt` | as-specified | `:211` |
| `Ingredients(...)` call forwards `getIngredientAt` | modified | The spec offered "add in the correct position **or** convert to named arguments"; Claude converted all 8 arguments to named form. Correct, and it accounts for most of the 60-line churn in that file. See Learning 6. |
| `Ingredients` signature gains `getIngredientAt` | as-specified | `:623` |
| `forEach` → `forEachIndexed` | as-specified | `:709` |
| `LaunchedEffect(Unit)` block replaced by the `BindLongClick` call | as-specified | `:751`; blank-result guard present, gesture still consumed, `SDK_INT <= S_V2` toast unchanged |
| `IngredientsPreview` / `RecipeDetailLayoutPreview` pass `getIngredientAt = { "" }` | as-specified | `:1010`, `:1097` |
| Imports: add `rememberUpdatedState`, remove `android.widget.TextView`, keep the rest | as-specified | Exactly the prescribed delta |
| `ScreenshotsTestSuite.kt` gains `getIngredientAt = { "" }` | as-specified | One added line |
| Test cases 1–4 appended to task 1's file, fixture reused | as-specified | Names and assertions verbatim; +29 lines to the existing file, no second test file, no renumbering |
| No `LaunchedEffect(Unit)` left around the long-click registration | as-specified | Only `LaunchedEffect(state)` at `:149` remains in the file |
| `./gradlew ktlintFormat` / `ktlintCheck` | as-specified | Both green, no violations |
| `./gradlew test` | as-specified | Green; XML inspected — `RecipeDetailViewModelUnitTest tests="6" failures="0"` |
| `./gradlew compileFullDebugAndroidTestKotlin` | as-specified | Green — the check CI cannot do |
| `./gradlew assembleFullRelease` | dropped | Cannot run on this machine: `validateSigningFullRelease` aborts because the Cryptomator vault holding `keystore.jks` is unmounted. Substituted with `compileFullReleaseKotlin` + `assembleFullDebug`, which covers the `@Preview` compile-check the spec actually wanted. Disclosed. See Learning 2. |
| Manual verification steps 1–6 | modified | Steps 1–6 need a Nextcloud server login the agent does not have. Substituted with a throwaway instrumented test on an API 29 emulator (which also exercises the `SDK_INT <= S_V2` toast path), plus the user's own on-device confirmation at 22:11Z. See Emerged Designs and Learning 4. |
| Conventional commit, `fix:` subject, #208 in the PR not the commit body | modified | Subject is exactly the spec's suggestion and #208 is referenced only in the PR. The body carries a `Co-Authored-By` attribution trailer the user's global instructions forbid. See Learning 1. |
| KDoc on `BindLongClick` | added | Explains why `rememberUpdatedState` is load-bearing and how twain's factory-block `TextView` creates the hazard — the spec's reasoning preserved at the call site |
| Throwaway instrumented test (`TmpLongPressCopyTest.kt`) | added | Created, run green, run red against the reverted fix, restored, deleted. Not in the commit. |
| `./gradlew lintFullDebug` | added | Not requested; green |
| `~/.gradle/init.d/nextcloud-cookbook-builddir.gradle` | added | Response to user message 3, not to the task spec. Correctly outside the repo. |
| Feature branch, push, PR #209 | added | Branch not mentioned in the task spec; PR opened on request with `Fixes #208` |

Nothing in the task spec was silently dropped — the one dropped command was reported.

## Spec Gaps

**The spec assumed the implementer could perform the manual verification.** Its Done-when section
makes manual steps 1–6 load-bearing ("required — instrumented tests do not run in CI") but never
says *who* runs them, and every step needs a logged-in Nextcloud server. The agent structurally
cannot do that. The spec simultaneously closes the obvious escape hatch — "do not add an instrumented
test to compensate unless you also arrange for it to run" — without saying what *does* count as
arranging for it to run. Claude found a good answer anyway (Emerged Designs), but it had to invent
the policy mid-task.

**The verification block was not checked against this machine.** `assembleFullRelease` had already
been recorded as unrunnable here in agent memory *before* this task was written, and the spec still
prescribed it. Same class of defect as task 1's `testDebugUnitTest`, one task later.

Everything else the spec asserted held. The pre-verified claims — that `DisposableEffect` runs late
enough for `findViewById`, that indices are stable across recalculation, that only
`rememberUpdatedState` is a new import, that `.editorconfig` exempts `@Composable` from the
function-naming rule — were all correct, and none cost a round trip.

## Over-Design

**The two-layer fix, already weighed and accepted by the spec.** `BindLongClick` alone closes #208;
`getIngredientAt` alone would close it only by convention. Both shipped. The cost is visible in the
diff: threading one parameter through two composables, two previews, and an androidTest file is most
of the +86/−14, for a lookup that is not what fixes the bug. The spec's §5 states the tradeoff
plainly and chooses testability, and the throwaway instrumented test then verified the layer the unit
tests cannot reach — so the accepted rationale ("the lookup is what makes it assertable in CI") did
hold up. Recorded as an accepted cost, not a defect.

No over-design attributable to the implementation. Nothing was added that the spec did not ask for
except the KDoc, the extra lint run, and the throwaway test — all of which paid for themselves.

## Under-Design

**The spec offered a choice it should have made.** On the positional `Ingredients(...)` call it said
"either add the new argument in the correct position or convert that call to named arguments." Both
are defensible; they differ by roughly 12 lines of diff in a hunk a reviewer has to read. A spec
precise enough to name line numbers and enumerate import deltas should have picked one.

**"Arrange for it to run" was left undefined.** The instrumented-test prohibition carried a condition
with no operational meaning. Claude's reading — write it, run it on a real emulator, prove it
discriminates, then delete it rather than commit a test CI will never execute — is the right one and
should be written down rather than re-derived.

## Code Review & Corrections

### User Message Classification

Messages 1–5 below are the task 2 portion. Message 1 of the session (task 1's prompt) is omitted.

| # | Timestamp | Intent | Summary |
|---|-----------|--------|---------|
| 1 | 2026-08-29T21:02:04Z | initial-prompt | `implement task-2-fix-stale-ingredient-copy.md` |
| 2 | 2026-08-29T21:23:50Z | instruction | `please also commit the specs` |
| 3 | 2026-08-29T21:30:30Z | instruction | Reports `:app:kaptFullDebugKotlin` failing when launching from Android Studio — opens an unplanned environment sub-task |
| 4 | 2026-08-29T21:59:22Z | correction | `are you doing anything? i've launched it manually in the meantime` — sent while the adb boot-wait loop was hung |
| 5 | 2026-08-29T22:11:20Z | approval | `i've tested the bugfix on my device and its working. please push and open a pr` |

Zero corrections and zero review feedback on the code. The only corrective message is #4, and it is
about Claude appearing stuck, not about the change.

Message 4 was enqueued rather than sent inline, so it is logged as a `queue-operation` plus an
`attachment` instead of a `type:"user"` entry — `extract-messages.sh` does not list it. Retros run
from that script will systematically under-count user interruptions.

### Review Findings

| # | Finding | Root Cause | Scope | Domain |
|---|---------|-----------|-------|--------|
| 1 | Commit `c815f10` carries a `Co-Authored-By` attribution trailer — the third on this branch | convention-miss | team | process |
| 2 | The spec's `assembleFullRelease` verification step cannot run on this machine | spec-wrong | project | tech-stack:gradle |
| 3 | `adb` invoked without `-s` with two emulators attached; the boot-wait loop hung to a 10-minute timeout | baseline-miss | team | process |
| 4 | The eCryptfs kapt failure was routed around privately in task 1 and only surfaced when the user hit it themselves | baseline-miss | team | process |
| 5 | Two emulators and 120 MB of orphaned `app/build` output left behind (disclosed, not cleaned) | baseline-miss | team | process |

## Emerged Designs

**The throwaway instrumented test — a discriminating end-to-end proof for a change CI cannot cover.**
This is the significant find of the task. The sequence:

1. Boot an API 29 emulator — deliberately chosen because it also exercises the `SDK_INT <= S_V2`
   toast branch that a modern device would skip.
2. Write `app/src/androidTest/.../TmpLongPressCopyTest.kt`, driving the real Compose tree: change the
   servings, long-press the actual interop `TextView`, read the clipboard.
3. Run it — green.
4. **Restore the original `LaunchedEffect(Unit)` capture and re-run** — red, with
   `org.junit.ComparisonFailure: clipboard after long-pressing "500 g flour" expected:<[5]00 g
   flour> but was:<[4]00 g flour>`. That is issue #208 reproduced exactly, which proves the test
   discriminates rather than merely passing.
5. Restore the fix from a scratchpad backup, re-run green, `grep`-verify the restore, delete the
   temp test.

This is task 1's mutation-check discipline escalated from the assertion level to the end-to-end
level, and it closes the exact hole the spec named as an accepted limitation ("the reported bug still
has no automated regression test — the ViewModel tests cover the lookup, not the Compose binding that
actually failed"). It also answers the spec's own prohibition correctly: the test *was* arranged to
run, it ran, and then it was deleted rather than committed as dead weight CI would never execute.

Worth promoting into the spec/task template as the default move whenever a change's real failure mode
lives outside CI's reach: **boot it, break it, prove the proof fails, restore, discard.**

**Measuring the environment instead of trusting a number.** The 143-byte eCryptfs cap was established
by `touch`ing filenames of increasing length in the actual working copy and comparing against `/tmp`,
not by citing a documented limit. The 8-character overshoot that followed was then a fact, not an
estimate — which is what made the three-option `AskUserQuestion` concrete enough to decide from.

**Checking for a library escape hatch before changing the user's machine.** Before proposing either
fix, the Dagger compiler jar was unzipped and its option classes read to confirm
`LazyClassKeyProcessingStep` writes those files unconditionally. Ruling out the in-band fix first is
what made a machine-level workaround the right call rather than a shortcut.

## Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | The `Co-Authored-By: Claude …` trailer appeared again on `c815f10`, and on `64fe11e` after it — three commits on one branch, in the same session, against a global instruction that says "Never add a `Co-Authored-By` trailer (or any other attribution trailer)". Task 1's retro already logged this. A recurrence inside a single session shows the harness template reliably beats the user's `CLAUDE.md` here, so restating the rule is not the fix; it needs to be enforced where commits are actually composed. | team | process | convention-miss |
| 2 | The spec prescribed `./gradlew assembleFullRelease` as a local verification step, which aborts on this machine at `validateSigningFullRelease` because the Cryptomator vault holding the keystore is unmounted — a fact already in agent memory before the spec was written. The spec wanted the `@Preview` compile-check; `compileFullReleaseKotlin` delivers that without a keystore. This is the second consecutive task whose verification block prescribed a command that cannot run as written (task 1: `testDebugUnitTest`). | project | tech-stack:gradle | spec-wrong |
| 3 | The eCryptfs kapt failure was diagnosed in task 1 and privately worked around with a per-invocation `--init-script`, without telling the user their machine had a build-blocking defect. The user hit the same failure in Android Studio 28 minutes after task 2 was committed and reported it as a new problem, costing a ~40-minute detour mid-task. An environment defect that blocks the user's own tooling should be surfaced the moment it is understood, not routed around silently. | team | process | baseline-miss |
| 4 | The spec made manual verification "required" for Done-when but every step needed a Nextcloud login the agent cannot have, named no owner for those steps, and forbade compensating with an instrumented test "unless you also arrange for it to run" without defining what that means. The gap was filled well (the throwaway discriminating test), which argues for writing that technique into the template rather than repeating the prohibition. | team | process | spec-gap |
| 5 | With two emulators attached, `adb wait-for-device` and the `sys.boot_completed` poll loop were issued without `-s <serial>`; adb refused with `error: more than one device/emulator`, the `until` loop spun for the full 10-minute timeout, and the user interrupted to ask whether anything was happening. A prior `adb emu kill` was also assumed to have succeeded and had not. Any adb call in a session that has booted an emulator must pin the serial and verify teardown rather than assume it. | team | process | baseline-miss |
| 6 | On the positional `Ingredients(...)` call the spec offered "add it in the correct position **or** convert to named arguments" and left the decision open. The larger-diff branch was taken — a good call, but it produced most of the churn in that file's hunk. A spec detailed enough to enumerate line numbers and import deltas should decide this itself rather than hand a reviewer-visible diff-size choice to the implementer. | project | process | under-design |
