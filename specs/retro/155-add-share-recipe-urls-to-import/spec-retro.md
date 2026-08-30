# Spec Retro: Share recipe URLs into the import screen

**Spec**: `specs/spec/155-add-share-recipe-urls-to-import.md`
**Tasks**: 3 (0 corrections across all three)
**Date**: 2026-08-30

## Summary

The cleanest spec in this repository so far: three tasks, nine user messages, **zero corrections**,
zero clarifications, zero rework, and not one file edited twice. Every one of the eleven architecture
decisions survived implementation, and the three riskiest — the Compose Destinations encoding claim
(§2.4), the load-bearing `currentDestination == null` gate (§2.6) and the back-stack `popUpTo` fix
(§2.7) — held because the spec verified them against library sources and generated code *before*
writing them down rather than reasoning about them.

The one decision that did not fully hold is §2.10, and it is the spec's only user-visible defect: the
section claims a re-shared recipe "lands on that snackbar instead of a raw error" and gets it for free
from PR #207. At the ViewModel layer that is true. At the snackbar the user actually sees it is not —
`RecipeRepositoryImpl.kt:237` passes `name = ""` on the import path, so the conflict renders the
generic `409 Conflict` string with no recipe name and no *View original* action. The spec reasoned
about the layer it was changing and inherited the layer beneath it unverified.

That is the same failure shape as the two smaller under-designs the task retros found, and it is the
single dominant pattern of this spec: **it states a two-part rule or an end-state, then verifies only
the part it is directly writing.** Three occurrences in three tasks.

| Metric | Value |
|--------|-------|
| Tasks | 3 |
| Total corrections | 0 |
| Tasks with 0 corrections | 3 |
| Total user messages | 9 (2 + 3 + 4) |
| Learnings by root cause | spec-gap: 5, spec-wrong: 1, over-design: 0, under-design: 3, baseline-miss: 4, convention-miss: 0 |
| Implementation footprint | 9 files, +360/−7 across `31f0295`, `db4ecf4`, `0c23bbd` |
| Acceptance criteria shipped unverified | 2 of 7 in task 3 (logged-out hold, `Spanned` payload) |

## Architecture Decisions Scorecard

| # | Decision | Score | Notes |
|---|----------|-------|-------|
| 2.1 | Single activity, intent handling stays in `MainActivity` | held-up | Task 3 wired `setIntent()` → `sharedTextState` → `NextcloudCookbookApp` exactly as written. No second activity, no XML, and the PR #167 reviewer's requested approach was honoured. |
| 2.2 | Responsibility split across the four layers | held-up | Each layer did its stated job; task 3's diff touches only `MainActivity`/`MainViewModel`/manifest and needed nothing from task 2's machinery corrected. The rationale for putting extraction in `DownloadRecipeViewModel` (the screen opens either way) proved right — the non-URL case needed no new branch anywhere. |
| 2.3 | URL extraction as a pure Kotlin extension, no `android.util.Patterns` | held-up | Task 1: 21/21 green on the plain JVM, no new dependency, `Patterns.WEB_URL` absent from the change. The bracket-balancing rule that reads like gold-plating was justified by two required inputs disagreeing on `]` and produced no wrong first draft. |
| 2.4 | The URL travels as a Compose Destinations navigation argument | held-up | The spec's riskiest claim — that the library encodes/decodes `String` args itself, making PR #167's `URLEncoder` surgery unnecessary — was confirmed end to end in task 2 by reading the KSP output: `"$baseRoute?sharedText={sharedText}"`, `serializeValue` on write, `stringNavType.get(savedStateHandle, …)` on read. The "no default value" instruction also held; `ScreenshotsTestSuite` compiled untouched. |
| 2.5 | One-shot delivery (`_sharedTextState` + `autoImportTriggered`) | held-up | Both guards landed, and task 3 confirmed case 5 (process death) on a device. The task-2 retro notes a consequence the spec never states: because the flag is written *before* the request, a process kill mid-flight leaves the screen waiting for a tap. Conservative and defensible — but §2.5 and §2.10 were reasoned about separately and never put side by side, where §2.10 shows an auto-retry would have been safe. |
| 2.6 | Navigation timing: wait for splash and credentials | held-up (half unverified) | The four-condition gate landed verbatim, and the paragraph arguing that `currentDestination == null` is load-bearing rather than defensive is why the shipped code has it *and* a comment protecting it from a future tidy-up. Requirement 6 (share while logged out) still falls out of the design as claimed, but case 3 was never run — see Spec Review #6. |
| 2.7 | Back-stack handling, including the `popUpTo` change as "a required fix, not a cleanup" | held-up | Confirmed twice: the user ran the recipe-list entry path manually at the end of task 2 ("landing on the list screen. Works."), and task 3 confirmed the cold-start share path lands on home, not back on the import screen. |
| 2.8 | `android:launchMode="singleTop"` | held-up | Verified in task 3 by the framework's own verdict rather than a screenshot — `am start` reporting *"delivered to currently running top-most instance"*, the same `ActivityRecord`, zero `onDestroy` in logcat. |
| 2.9 | No `android:label` on the share-sheet filter | held-up | Case 10 run through a real browser share sheet: the app appears as "Cookbook" with its own icon. No string resource added. |
| 2.10 | Re-sharing an already imported recipe needs "no extra work" | **partially-wrong** | Correct at the ViewModel layer (409 → `ConflictState.Active` + reset to `Initial(url)`, both confirmed on a live server) and wrong at the layer the user sees. `recipe/data/repository/RecipeRepositoryImpl.kt:237` calls `handle409ConflictError(response, name = "")`; a blank name skips the previews lookup, so `existingRecipe` is null, `RecipeConflictDto(id = null, name = "")` comes out, and `ConflictSnackbar` drops the *View original* action. Pre-existing (`f54ddfd`, PR #207), below all three 155 commits, and explicitly deferred by the user. |
| 2.11 | Extend `DownloadRecipeViewModelUnitTest`; real `SavedStateHandle`; follow *this* file's style | held-up | Task 2 extended rather than forked, kept the four pre-existing cases untouched apart from `setUp()`, used a real `SavedStateHandle(mapOf(...))` with no mocking, and followed the local backtick/`runTest` style rather than the `StringAddSuffixUnitTest` style task 1 had just used. The claim that `kotlinx-coroutines-test` was already on `main` was correct — no `app/build.gradle` change anywhere. |

Ten held up, one partially wrong. Nothing was replaced or unwound.

## Task Breakdown Assessment

| Task | Corrections | Messages | Assessment |
|------|-------------|----------|------------|
| Task 1: `String.extractHttpUrl()` URL extraction | 0 | 2 (`implement …`, `commit changes`) | well-sized — 3 min 51 s, 2 files, +158/−0 |
| Task 2: `sharedText` nav argument and auto-import | 0 | 3 (`implement …`, manual-check report, `commit this`) | well-sized — ~3 min agent time, 4 files, +143/−4 |
| Task 3: Share target registration and gated navigation | 0 | 4 (`implement …`, a URL for case 9, `Leave task 3. Commit changes.`, `push and open a pr`) | well-sized — ~1 min implementing, ~10 min verifying, 3 files |

**Right-sizing.** All three tasks are correction-free, which is the strongest possible signal. The
message counts are misleading if read as iteration: of the six non-initial messages, three are commit
instructions the task docs should have made unnecessary (see Spec Review #1), one is the user running a
manual check the task doc *correctly* assigned to a human, one supplies a live-library URL the agent
could not know, and one is a scope decision. **None is a correction, a clarification of ambiguity, or
review feedback.**

**Dependencies.** The README's claim that "Task 2 cannot compile without task 1's extension function,
and task 3 cannot compile without task 2's navigation argument" is exactly right, and the strict order
was correct. No task needed work from a later one; no hidden dependency surfaced. Each slice left `main`
shippable — tasks 1 and 2 are user-invisible apart from the §2.7 back-stack fix, and task 3 is the only
one that makes the feature reachable.

**Missing tasks.** None on the code. The three slices cover the whole chain, and the diff contains
nothing the spec did not ask for. Two process items were missing rather than mis-sized: no task doc
carries a `## Commit` section, and no task owns proving the logged-out path (requirement 6), which the
spec claims "falls out for free" from §2.6 and which nothing then checked.

**Merges/splits.** None warranted. Task 1 is small but genuinely independent — a pure function with a
21-row contract, no Android, no dependants — and merging it into task 2 would have put the riskiest
parsing logic behind a ViewModel that also needed device verification. The three-way cut is the right
shape and should be reused for feature chains of this form.

## Spec Review

| # | What the spec said | What happened | What the spec should have said |
|---|-------------------|---------------|-------------------------------|
| 1 | **Missing: no `## Commit` section in any of the three task docs.** All five task specs in the 183 and 208 breakdowns have one. | The user had to send a separate message in all three sessions — `commit changes`, `commit this`, `Leave task 3. Commit changes.` Three sessions, three extra round trips, one per task. | Each task doc ends with the standard `## Commit` section naming the conventional-commit subject, e.g. ``## Commit``  /  ``feat: extract the first http(s) URL from a string`` — subject only, no body, no trailer. This is a template regression for spec 155 as a whole, not three independent oversights, so the fix belongs in the task-breakdown template rather than in these three files. |
| 2 | Task 1 §Verification: ```./gradlew ktlintCheck test``` … *"confirm it by name rather than assuming, e.g. by checking `app/build/reports/tests/` or running with `--tests '*StringExtractHttpUrl*'`."* | Gradle rejected it: *"Problem configuring task :app:test from command line. > Unknown command-line option '--tests'."* `test` is a lifecycle task and takes no `--tests`; only a concrete `Test` task does. Recovered as `testFullDebugUnitTest --tests`. Second flavour-related wrong Gradle command a spec has prescribed here — the 208 retro logged `testDebugUnitTest` for the same reason. The `app/build/reports/tests/` path is also wrong in this checkout. | *"Run `./gradlew ktlintCheck test`. To confirm the new class actually ran, use a concrete test task — `./gradlew testFullDebugUnitTest --tests '*StringExtractHttpUrl*'` — or read the JUnit result XML directly; `test` is a lifecycle task and rejects `--tests`."* The root cause is upstream: `CLAUDE.md:19` still lists `./gradlew testDebugUnitTest`, which does not exist in a flavoured build, and specs keep inheriting it. Fix `CLAUDE.md` and both spec bugs stop recurring. |
| 3 | §3.2: *"Returns `null` when nothing matches **or** when the remainder after the scheme is empty."* §4.1 assigns `\| "https://" \| null \|` to the second condition. | It does not reach it. The `\S+` the spec also prescribes requires at least one character after `://`, so `"https://"` produces no match and returns at the earlier `?: return null`. The shipped `takeIf { it.substringAfter("://").isNotEmpty() }` guard has **no covering test** and could be deleted with all 21 rows still green — though it is not redundant: `"Look at https://."` strips to exactly that case. | Keep both conditions, and give the second a row that reaches it: `\| "Look at https://." \| null \|` — a match that survives the regex and is emptied by the strip loop. The generalisable rule: when a spec fixes both the implementation technique and the test table, trace at least one input per stated rule *through the prescribed technique* to confirm the row reaches the clause it was written for. |
| 4 | §3.6: *"read `savedStateHandle.get<String>("sharedText")`; do nothing when null/blank"*. §4.2's table has an empty-`SavedStateHandle` row but no present-but-blank row. | The implementation correctly wrote `if (!sharedText.isNullOrBlank())`, but only the null half is covered. Swapping it for `isNullOrEmpty()` leaves all twelve tests green while changing real behaviour: a whitespace-only share (`"   "`, or a stray newline from a share sheet) would seed the form with whitespace instead of an empty field. | Add a §4.2 row: *`sharedText` = `"   "` → construct the ViewModel → `uiState` is `Initial(url = "   ")`, repository never called.* Same rule as #3: every clause of a prescribed guard needs its own row. |
| 5 | §2.10: *"Sharing the same page twice … therefore lands on that snackbar instead of a raw error, and the automatic import needs no special case for it."* §4.3 case 9: *"the conflict snackbar appears with the recipe name; *View original* opens it."* | Run against the user's live library in task 3, the 409 fires and the ViewModel behaves exactly as §2.10 says — but the snackbar is the generic `409 Conflict: Recipe already exists`, with no recipe name and no action. `RecipeRepositoryImpl.kt:237` passes `name = ""` on the import path; a blank name skips the previews lookup that would supply the id, so `ConflictSnackbar` drops the action label. Pre-existing since `f54ddfd`; the agent proved it pre-existing by reproducing it through the manual Download button and `git blame`ing the line, then asked, and the user deferred it. | §2.10 should have traced the value that populates the snackbar back to its source before claiming the outcome: *"PR #207 translates the 409 into `ConflictState.Active`. Note that `RecipeRepositoryImpl` currently calls `handle409ConflictError(response, name = "")` on the import path, so the snackbar renders the generic string without the recipe name or *View original*; the ViewModel behaviour is reused as-is and improving the snackbar is out of scope."* Then case 9 asserts the generic snackbar, and nothing ships against a promise the code cannot keep. A spec section whose content is "we get this for free from an earlier PR" is an *inherited assumption*, not a decision, and needs verifying like new code. |
| 6 | Task 3 acceptance criteria: *"Sharing while logged out holds the URL and imports after the login flow completes"* and *"Styled text (a `Spanned` payload) is not silently dropped"*. §4.3 case 3 is marked *"must not be skipped"*. The only tool supplied is the `adb shell am start … --es` line. | Neither criterion is reachable by the method the spec supplies. Case 3 needs the emulator signed *out* of a real Nextcloud account and then back *in*; the agent stopped at *"I don't have the credentials to sign back in"*, which was the right call. And `adb --es` can only put a `String` in `EXTRA_TEXT`, so the `getCharSequenceExtra`-vs-`getStringExtra` distinction the spec argues for at length is invisible to it. Both shipped on code inspection only; a regression in either would be silent. | Every acceptance criterion needs a stated route to checking it, or an explicit *"code-review only, no runtime verification available"* marker. For case 3: name the mock-Nextcloud sign-in harness built during the #183 task-2 work (see `emulator-signin-harness-needs-adb-reverse`) as the route — it existed, but the memory recording it was written four hours after this session. For `Spanned`: either drop it as an acceptance criterion and keep it as a code-review note, or supply a route (a tiny sender app or the Robolectric test §4.3 rules out). |
| 7 | Task 3 §Verification: ```./gradlew ktlintCheck test lint``` — parent spec §4.4 additionally requires `./gradlew compileFullDebugAndroidTestKotlin`, *"CI never compiles the `androidTest` source set, so a break there is easy to miss."* Task 2's doc carried it; task 3's dropped it. | Task 3 is the task that changes the `NextcloudCookbookApp` signature, so it is exactly where an `androidTest` break was most likely. No cost was incurred — the session went back to §4.4 on its own and ran it — but the omission was real, and relying on the agent to re-read the parent spec is not a control. | Task 3's Verification block should read ```./gradlew ktlintCheck test lint compileFullDebugAndroidTestKotlin```, with §4.4's one-line reason. More generally: a task doc may *add* to the parent spec's build checks but must never silently narrow them; if the breakdown distributes §4.4 across tasks, it should say so and say which task carries which check. |

## Undocumented Conventions

**No `convention-miss` learnings were recorded in any of the three task retros.** The spec pre-resolved
every convention question it touched, and cited the precedent inline each time it did: constructor
parameter ordering against `RecipeDetailViewModel`/`RecipeListViewModel`, the `@Suppress("UNUSED_PARAMETER")`
nav-argument shape against `RecipeDetailScreen`, the pure-extension-plus-JVM-test shape against
`StringAddSuffixExtension.kt`, the test-file style against `DownloadRecipeViewModelUnitTest` explicitly
in preference to the newer #209/#210 harness, and the flat test package against the source tree layout.
Zero convention questions went back to the user across three sessions.

One documentation defect is worth carrying to meta-retro even though it is not a `convention-miss`:

| # | Item | Evidence | Where to document |
|---|------|----------|-------------------|
| 1 | `CLAUDE.md` documents a Gradle test command that does not exist in this flavoured build. Line 19 lists `./gradlew testDebugUnitTest`; with the `full`/`googlePlay` flavours the concrete tasks are `testFullDebugUnitTest` / `testGooglePlayDebugUnitTest`, and `test` is a lifecycle task that rejects `--tests`. | Task 1 Learning 1 (`spec-wrong`) — the spec inherited the bad command from `CLAUDE.md` and Gradle rejected it mid-session. The 208 retro logged the same underlying defect. Two specs, two breakdowns, same root. | Root `CLAUDE.md`, Testing section — replace `testDebugUnitTest` with the flavoured task names and note that `--tests` requires a concrete `Test` task. |
| 2 | Where relocated build output actually lands (`/var/tmp/ncc-build/_app/…`, module directory prefixed `_`) — guessed wrong in both task 1 (`app`) and task 2 (`_`), costing a `find` each time. | Task 1 L2, Task 2 L3, Task 3 L4 — all `baseline-miss`, all three sessions. | Already fixed: the `kapt-fails-on-ecryptfs-home` memory was corrected during task 3 and now records the permanent `~/.gradle/init.d/` relocation and the `_app` path. Per `keep-local-setup-out-of-specs` this stays out of committed specs. No further action. |

## What Went Well

**Zero corrections across three tasks.** No spec in this repository has managed that before. Nine user
messages produced three commits, a push and a PR, with no line of implementation revised after it was
written and no file edited twice.

**The spec verified its own risky claims before prescribing them, and that is why prescription worked.**
Task 2's retro makes the point precisely: the task docs are unusually prescriptive — they fix constructor
parameter order, the exact statement sequence in an `init` block, local variable names, which of two
`Intent` accessors to call, and eight test rows verbatim. That level of prescription is normally a
liability. It paid off here because every prescription had already been checked against something real:
the parameter ordering against two sibling ViewModels, the encoding claim against the Compose
Destinations sources, the call-site count against a repo-wide grep, the `@Suppress` against
`RecipeDetailScreen`. Prescription is not free — it worked because the verification was done first.

**Three traps were called out in advance and none was hit.** The `init`-block ordering trap (seed
`_uiState` before `importRecipe()`), the stub-before-construct trap in the test file, and the
`destinationsNavigator` shadowing trap in `MainActivity`. Each was flagged in prose; each was avoided
without a round trip. The §2.6 `currentDestination == null` paragraph is the best example of the form —
a check that looks defensive, argued at length as load-bearing, and shipped *with a comment* so the next
person to tidy up a "redundant null check" cannot silently delete the fix.

**The breakdown README's "spec corrections already applied" section did its job.** Three claims from the
spec's first draft were wrong and were recorded as such so they could not be re-introduced from memory
of the earlier version. None of them reappeared.

**Scope discipline in every session.** The working tree carried an unrelated `fastlane/Fastfile`
modification throughout; all three sessions diffed it, recognised it as unrelated, and staged by explicit
path. All three commits use conventional subjects with no body and — unlike all three commits reviewed in
the 208 retro — **no `Co-Authored-By` trailer**.

**Practices transferred between sessions without being asked for.** Task 1 invented asserting on the
JUnit result XML (`tests="21" skipped="0" failures="0" errors="0"` plus the full `testcase name=` list)
rather than on `BUILD SUCCESSFUL`; task 2 reused it unprompted. Task 3 went back to the parent spec's
§4.4 to recover a build check its own task doc had dropped. Two independent instances of a practice
propagating on its own is a strong argument for writing both into the spec template's verification
guidance rather than relying on rediscovery.

**Three emerged designs that improved on the spec.** Reading the KSP-generated destination file to prove
a nav-argument contract end to end (the cheapest possible proof, free after any build that runs KSP);
anchored `python3` replacements with `assert old in s` and `count(...) == 1`, which is what makes
shell-driven editing as safe as the Edit tool; and task 3's regression-vs-pre-existing protocol —
reproduce through the old path, locate the responsible line, `git blame` it, confirm with `merge-base`,
*then* ask about scope. The user answered that one in six words.

**Task 3 drove its own device verification.** Eight of ten manual cases in roughly eight minutes against
a real Nextcloud server, with two evidence habits worth keeping: using the framework's own logcat verdict
for `singleTop` rather than a screenshot, and asserting **pixel-identical** screenshots for
back-navigation rather than "looks right". It then stated plainly what it had not run and why, and wrote
the known gaps into the PR body rather than omitting them.

## Spec-Level Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | Parent spec §4.4 mandates four build checks (`ktlintCheck`, `test`, `lint`, `compileFullDebugAndroidTestKotlin`). The breakdown silently redistributed them and **no task doc carries the full set**: task 1 has `ktlintCheck test`, task 2 has `ktlintCheck test compileFullDebugAndroidTestKotlin`, task 3 has `ktlintCheck test lint`. The redistribution is invisible from inside any single task, and it left the largest-surface task — the only one changing a composable signature — without the `androidTest` compile check. Nothing broke only because the task-3 session re-read §4.4 of its own accord. When a breakdown narrows a parent spec's verification set, it must either repeat the full set in every task or state the distribution explicitly in the README's Scope notes. | team | process | spec-gap |
| 2 | The breakdown's Approach section says the manual layer "lands last" but never says **who runs it**, and the answer differed per task with no plan behind it: tasks 1 and 2 handed the device work back to the user (task 2's wall clock is 34 minutes of which 31 are the agent idle, against ~3 minutes of actual work), while task 3 installed the build and drove the emulator itself. The same capability was available in all three sessions. A breakdown that defers verification to a human should say so, because the earlier slices' acceptance criteria then block on a person being at the keyboard; and if the agent can drive a device, that should be the stated default rather than a per-session discovery. | team | process | spec-gap |
| 3 | The spec makes three "this comes for free" claims — §2.11 (`kotlinx-coroutines-test` already on `main` from #207), §3.8 (no `app/build.gradle` change needed) and §2.10 (the conflict snackbar is inherited from #207). The two **mechanical** claims were true and cost nothing. The one **behavioural** claim was false at the layer the user sees, and it is the feature's only shipped defect. Mechanical inheritance (a dependency exists, a file needs no change) is verifiable by inspection and safe to assert; behavioural inheritance (an earlier PR already produces the right user-visible outcome) is a full end-to-end claim wearing the costume of a no-op, and needs the same verification budget as new code. Flag behavioural "for free" sections and require one traced call chain each. | team | process | under-design |
| 4 | The three-slice cut produces excellent per-layer tests — 21 pure-string cases and 12 ViewModel cases — but **no automated test crosses a layer boundary**, so the feature's headline behaviour (a share intent arriving and ending on a running import) has no regression test at all. The spec ruled out Robolectric deliberately and defensibly (§4.3), and the manual walkthrough was thorough; the gap is that the walkthrough is not re-runnable, so every criterion it covers degrades to code review on the next change to `MainActivity` or the nav gate. This is an accepted trade-off, but the spec never states it as one. A spec that declines integration testing should say which criteria are consequently protected only by a one-time manual pass. | team | process | under-design |
| 5 | Requirement 6 (share while logged out) is the only functional requirement with **no owner anywhere in the breakdown**. §2.6 says it "falls out for free" from the credentials gate, no task doc claims it as a deliverable, task 3 lists it as an acceptance criterion, and case 3 — marked "must not be skipped" — was the one case never run. A requirement that a design section claims as an emergent consequence is exactly the kind that no task feels responsible for proving; the breakdown should assign every numbered functional requirement to a task explicitly, including the ones the architecture is expected to satisfy incidentally. | team | process | spec-gap |
