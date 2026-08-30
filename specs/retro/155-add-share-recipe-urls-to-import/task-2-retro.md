# Task Retro: `sharedText` nav argument and automatic import

**Task spec**: `specs/tasks/155-add-share-recipe-urls-to-import/task-2-shared-text-nav-argument.md`
**Sessions analyzed**: 1 (`7638380b`, branch `155-add-share-recipe-urls-to-import`)
**Date**: 2026-08-30

## Session Stats

The session covers task 2 only, start to commit. The commit was authored at 13:59:57Z as `ff6e79c`
and appears in the branch today as `db4ecf4` after a later rebase; the content is unchanged.

| Metric | Value |
|--------|-------|
| Sessions | 1 |
| User messages (iterations) | 3 |
| Errors encountered | 2 |
| Duration (wall clock) | 34 min 21 s (13:25:39.551Z → 14:00:00.601Z) |
| Duration (agent working) | ~3 min — 2 min 25 s implementing (13:25:44Z → 13:28:09Z) plus 35 s committing |
| Input tokens | 44 |
| Output tokens | 12,226 |
| Cache read tokens | 1,460,930 |
| Cache creation tokens | 55,292 |
| Questions asked (AskUserQuestion) | 0 |
| Subagent spawns (Task) | 0 |
| Tool calls | 19 Bash (0 Edit, 0 Write — all four files edited through anchored `python3` heredocs) |

**The 34-minute wall clock is misleading.** Implementation, verification and a written summary were
finished 2 min 25 s after the first prompt. The next 31 minutes are the user performing the manual
back-stack check on a device; the session was idle. Task 2 was the same size of job as task 1 and
took roughly the same agent time (~3 min vs. 3 min 51 s) despite touching four files instead of two.

Error breakdown:

1. **Exit 1** — `grep -rn "DownloadRecipeScreenDestination" --include=*.kt app/src/` failed with
   `(eval):1: no matches found: --include=*.kt`. zsh glob-expands the unquoted `*.kt` inside the
   option and aborts on no match. Retried without `--include` at all. One round trip. See Learning 4.
2. **Exit 2** — `ls /var/tmp/ncc-build/_/reports/tests/` on a guessed path; the relocated layout
   names the module directory `_app`, not `_`. Recovered with `find`. See Learning 3.

Neither error touched the code under change.

## What Went Well

**Three messages, zero corrections.** `implement <task spec path>`, then the user's own report that
the manual check passed, then `commit this`. No clarifications, no review feedback, no questions back
to the user, no rework — not one file was edited more than once.

**The spec's riskiest claim was verified against generated code, not assumed.** §2.4 rests on the
assertion that Compose Destinations 2.3.0 encodes `String` nav args itself and that reading
`savedStateHandle["sharedText"]` returns the same value `argsFrom(savedStateHandle)` would — the
claim that makes PR #167's `URLEncoder`/`URLDecoder` surgery unnecessary. After the build, the
session grepped the KSP output at
`/var/tmp/ncc-build/_app/generated/ksp/fullDebug/kotlin/…/DownloadRecipeScreenDestination.kt` and
found exactly that:

```kotlin
override val route: String = "$baseRoute?sharedText={sharedText}"
…
"?sharedText=${stringNavType.serializeValue("sharedText", sharedText)}"
…
sharedText = stringNavType.get(savedStateHandle, "sharedText"),
```

That is the spec's argument confirmed end to end — serialisation on write, the same `stringNavType`
on read — rather than taken on trust. See Emerged Designs.

**The test-XML confirmation pattern carried over from task 1.** Instead of reading `BUILD SUCCESSFUL`,
the session parsed `tests="12" skipped="0" failures="0" errors="0"` and printed all twelve
`testcase name=` lines out of the JUnit result XML, proving the four pre-existing cases and all eight
new ones ran and none was skipped. Task 1 invented this; task 2 reused it unprompted. It is the first
practice in this breakdown to transfer between sessions on its own.

**The verification block was run in full, including the step CI does not cover.** `ktlintCheck`
green in 2 s; `test compileFullDebugAndroidTestKotlin` green in 10 s. §4.4's prediction held —
`ScreenshotsTestSuite` never reaches `DownloadRecipeScreen`, so the new argument did not break the
`androidTest` source set and no default value was needed to rescue it.

**The one step it could not do, it said it could not do.** The closing summary opened with *"Not
done: the manual back-stack check in the running app … That needs a device or emulator — say the word
and I'll drive it via /run."* No claim of completion, and a concrete offer. The user ran it and
replied *"I've checked this manually and I'm landing on the list screen. Works."* This is the
behaviour the task-1 retro's verification learnings were pushing toward, arrived at without being
asked.

**Edits were anchored, not positional.** Every `python3` replacement was guarded by
`assert old in s` (and `assert s2.count(old_nav) == 1` for the single call site) before writing. A
drifted anchor would have failed loudly instead of silently mangling a file. With zero Edit/Write
tool calls in the session, that guard is the only thing standing between a stale assumption and a
corrupted source file.

**A speculative import was caught before the linter saw it.** The test-file edit added
`import org.junit.Assert.assertNull` for an assertion the final cases did not use; the session
noticed and removed it in a follow-up script, before `ktlintCheck` ran.

**Commit hygiene, again.** Conventional `feat:` subject, no body, and **no `Co-Authored-By` trailer**
— matching task 1 and the global instruction. The working tree carried an unrelated
`fastlane/Fastfile` modification (visible in the session's own `git diff --stat` as a fifth file);
the four task files were staged by explicit path and the Fastfile was left out.

**Result: 4 files, +143/−4, and no later commit on the branch has touched any of them.** Task 3
(`0c23bbd`) changed only `AndroidManifest.xml`, `MainActivity.kt` and `MainViewModel.kt` — it built on
task 2's machinery without having to correct any of it.

## Spec Accuracy

| Deliverable | Status | Notes |
|-------------|--------|-------|
| `savedStateHandle: SavedStateHandle` after `recipeRepository`, no `private val` | as-specified | Matches `RecipeDetailViewModel` / `RecipeListViewModel` |
| `init` reads `sharedText`, no-op when null or blank | as-specified | Written `val sharedText: String? = savedStateHandle["sharedText"]` rather than the spec's `get<String>(…)`; identical call, and the annotation is what makes the indexed form type-check |
| `val url = sharedText.extractHttpUrl()` | as-specified | Task 1's extension, imported from `core.util` |
| Seed `_uiState` with `Initial(url = url ?: sharedText)` | as-specified | Written `_uiState.value =`, whereas the file's five other `_uiState` writes use `.update { }`. No behavioural difference — nothing races during construction — but it is the one line in the change that does not read like its neighbours |
| Guard on `url != null` **and** flag not already `true`; write flag; call `importRecipe()` | as-specified | `savedStateHandle.get<Boolean>("autoImportTriggered") != true`, then `savedStateHandle["autoImportTriggered"] = true` |
| Seed state **before** calling `importRecipe()` | as-specified | The ordering trap the spec flagged twice was not hit |
| `importRecipe()`, `handleConflict()`, `dismissConflict()`, `_conflict` untouched | as-specified | Diff shows no line changed below the `init` block |
| No special case for a 409 on a re-shared URL | as-specified | The automatic import goes through the ordinary path; the conflict test proves the snackbar state falls out |
| `@Suppress("UNUSED_PARAMETER") sharedText: String?` between `navigator` and `viewModel`, no default | as-specified | Mirrors `RecipeDetailScreen`'s `recipeId` |
| `popUpTo(DownloadRecipeScreenDestination) { inclusive = true }` in `onNavigateToDetail` | as-specified | |
| Drop the now-unused `RecipeListScreenDestination` import | as-specified | The replacement `DownloadRecipeScreenDestination` import was added too — implied by the change, not stated by the spec |
| `DownloadRecipeScreenContent`, `DownloadRecipeForm`, conflict wiring, `@Preview` untouched | as-specified | `sharedText` appears nowhere below the destination composable |
| `RecipeListScreen` passes `sharedText = null` | as-specified | Single call site, confirmed by repo-wide grep before editing |
| No manual URL encoding anywhere | as-specified | No `URLEncoder`/`URLDecoder`/`"sharedText="` string surgery; the generated code was read instead |
| Extend `DownloadRecipeViewModelUnitTest`, no second file | as-specified | |
| `setUp()` gains an empty `SavedStateHandle()` | modified | Routed through the new `createViewModel(SavedStateHandle())` helper instead of an inline constructor call. Tidier than the spec's literal wording and leaves one construction site |
| `createViewModel(savedStateHandle)` helper | as-specified | Placed after the new cases, before the `conflictResource` doc block |
| Stub the repository **before** constructing | as-specified | Every case that expects an import stubs first; the ordering trap was not hit |
| Eight new cases per the §4.2 table | as-specified | All eight present, one per row, in the table's order; XML confirms `tests="12" … failures="0"` |
| Existing style: backtick names, `runTest { }`, `UnconfinedTestDispatcher`, `conflictResource` | as-specified | Not the `*UnitTest` snake-case style of `StringExtractHttpUrlUnitTest` from task 1 |
| The four pre-existing cases pass unmodified apart from `setUp()` | as-specified | Diff touches none of their bodies |
| No `app/build.gradle` change | as-specified | |
| Verification `ktlintCheck test compileFullDebugAndroidTestKotlin` | as-specified | Both green; `androidTest` compiled, so the new argument does not reach `ScreenshotsTestSuite` |
| Manual back-stack check from the recipe list | as-specified | Performed by the user, not the agent; reported as passing |
| Commit | added | The task spec has no `## Commit` section; the user asked in a third message |
| Inspecting the KSP-generated destination for the `sharedText` route | added | Not asked for; it is what turned §2.4 from an assertion into a verified fact |

Nothing in the task spec was dropped.

## Spec Gaps

**None on behaviour.** The task file specified the parameter position, the exact `init` sequence, the
ordering constraint, the file that must not be touched, the single call site, the test style and all
eight test rows. Every one of them landed, and not a single question went back to the user. This is
the second task in the breakdown implemented with zero clarifications.

**The same process gap as task 1.** No `## Commit` section, so the user sent `commit this` as a third
message. All five task specs in the 183 and 208 breakdowns have one; none of the three in this
breakdown does. Task 1's retro logged this; task 2 confirms it is a template regression rather than a
one-off. See Learning 2.

The spec correctly kept the eCryptfs build-directory workaround out of the committed file per
`keep-local-setup-out-of-specs`. The build-directory friction that occurred came from agent memory
instead — see Learning 3.

## Over-Design

**Nothing attributable to the spec.** The task file is unusually prescriptive: it fixes the
constructor parameter order, the exact statement sequence in `init`, the storage location of the
guard flag, the wording of the `@Suppress`, the absence of a default value, the test helper's
signature and eight test rows verbatim. On a less well-understood change that would be a liability.
Here every prescription was checked against the codebase before being written down — the parameter
ordering against two sibling ViewModels, the `@Suppress` against `RecipeDetailScreen`, the call-site
count against a repo-wide grep, the encoding claim against the library sources — and the result is a
2 min 25 s implementation with no corrections and nothing to unwind.

The cost is that the session made almost no design decisions of its own. The only ones it did make —
routing `setUp()` through the helper, reading the generated destination file — both improved on the
spec. Worth remembering when judging whether this level of prescription is repeatable: it worked
because the spec did the verification first, not because prescription is free.

The `autoImportTriggered` flag deserves a note because it *looks* like belt-and-braces next to
`MainViewModel`'s one-shot `_sharedTextState` (§2.5). It is not: the two guards close different
hazards at different layers, and the spec argues that explicitly. It earned its place.

## Under-Design

**The `isBlank` half of the guard has no covering test.** The spec says the `init` block must do
nothing when `sharedText` is *"`null` or blank"*, and the implementation duly writes
`if (!sharedText.isNullOrBlank())`. The §4.2 table covers the `null` half — the empty
`SavedStateHandle()` row — but no row supplies a present-but-blank payload. `Initial.url` defaults to
`""`, so replacing `isNullOrBlank()` with `isNullOrEmpty()` leaves all twelve tests green while
changing real behaviour: a whitespace-only share (`"   "`, or a stray newline from a share sheet)
would then seed the form with whitespace instead of an empty field. Verified against the shipped
guard and `DownloadRecipeScreenState.Initial`'s default while writing this retro.

This is the same shape of gap as task 1's — a spec that states two conditions, then derives test rows
for one of them and assumes the other is covered. Two occurrences in two tasks makes it a pattern
worth fixing at the spec-template level. See Learning 1.

**A mid-flight process kill cannot auto-retry, and the spec never says so.** §2.5 justifies
`autoImportTriggered` purely as a duplicate-import guard, and the flag is written *before*
`importRecipe()` runs. If the process dies while the request is in flight, the restored screen shows
`Initial(url)` with the flag already `true` and waits for a tap. That is the conservative outcome and
is defensible — but §2.10 means an auto-retry would have been safe too, since a 409 on an
already-imported recipe renders the friendly conflict snackbar rather than an error. The spec reasoned
about the flag and about the 409 path in separate sections and never put them side by side. **No
change is warranted** — the shipped behaviour costs one tap in a rare case — but it is the second
place in this task where two correctly-analysed rules were never checked against each other.

## Code Review & Corrections

### User Message Classification

| # | Timestamp | Intent | Summary |
|---|-----------|--------|---------|
| 1 | 2026-08-30T13:25:42Z | initial-prompt | `implement specs/tasks/155-add-share-recipe-urls-to-import/task-2-shared-text-nav-argument.md` |
| 2 | 2026-08-30T13:59:20Z | approval | *"I've checked this manually and I'm landing on the list screen. Works."* — the user ran the manual back-stack check the agent had flagged as not done |
| 3 | 2026-08-30T13:59:43Z | instruction | `commit this` |

Zero corrections, zero clarifications, zero review feedback. Message 2 exists only because the task
spec's verification block includes a step that needs a device — it is the spec working as intended,
not a defect.

### Review Findings

| # | Finding | Root Cause | Scope | Domain |
|---|---------|-----------|-------|--------|
| 1 | The `isNullOrBlank()` guard's blank half is untested; swapping it for `isNullOrEmpty()` keeps all 12 tests green | under-design | team | process |
| 2 | No `## Commit` section in the task spec — second occurrence in this breakdown | spec-gap | project | process |
| 3 | A redundant build-dir init script was written and passed from a then-stale memory note; its `-DrelocatedBuildDir` had no effect and the report path was guessed wrong | baseline-miss | project | process |
| 4 | `grep --include=*.kt` unquoted fails under zsh with `no matches found` | baseline-miss | team | tech-stack:zsh |

## Emerged Designs

**Reading the KSP-generated destination to verify a navigation contract.** After the build, grepping
`…/generated/ksp/fullDebug/kotlin/…/DownloadRecipeScreenDestination.kt` for `sharedText` showed the
generated route (`"$baseRoute?sharedText={sharedText}"`), the `serializeValue` call on the write side
and the `stringNavType.get(savedStateHandle, …)` on the read side. This is the cheapest possible
proof that a nav argument is wired the way a spec claims, and it is available for free after any
build that runs KSP. It belongs in the verification guidance for every future task that adds or
changes a Compose Destinations argument — reading generated code beats reasoning about the generator.

**Anchored `python3` replacements with `assert old in s`.** With every file edit going through the
shell rather than the Edit tool, an unmatched anchor would otherwise write a silently unchanged file
and the session would proceed on a false premise. Asserting the anchor first — and asserting
`count(...) == 1` for the single call site — turns that into an immediate loud failure. Cheap, and it
is what makes shell-driven editing as safe as the dedicated tool.

**Reusing task 1's JUnit-XML assertion without being told to.** The pattern was invented one session
earlier and neither the task spec nor the user mentioned it. Its reappearance suggests it is
discoverable enough to be worth writing into the spec template's verification section rather than
relying on rediscovery.

## Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | The spec required the `init` block to skip a `sharedText` that is *"null or blank"* and prescribed a test table that covers only the null half. No row supplies a present-but-blank payload, so the shipped `isNullOrBlank()` could be weakened to `isNullOrEmpty()` with all twelve tests still green — changing what a whitespace-only share does to the form. This is the second time in this breakdown that a spec stated a two-part rule and derived cases for one part only (task 1: the `"https://"` row could not reach the empty-remainder guard it was chosen to cover). When a spec fixes both the guard and the test table, every clause of the guard needs its own row, and each row should be traced through the prescribed implementation to confirm it reaches the clause it was written for. | team | process | under-design |
| 2 | The task spec has no `## Commit` section, so the user had to send `commit this` as a separate message — the same friction task 1 hit. All five task specs in the 183 and 208 breakdowns have one and none of the three in the 155 breakdown does, confirming a regression in the task-breakdown template rather than a per-task oversight. Two confirmed occurrences in one breakdown; task 3 will make three. | project | process | spec-gap |
| 3 | The session read the `kapt-fails-on-ecryptfs-home` memory, which at that moment still prescribed relocating the build directory via `./gradlew --init-script <script> -DrelocatedBuildDir=…`, wrote that script and passed it. It had no effect: the permanent `~/.gradle/init.d/` script won, and output landed under `/var/tmp/ncc-build/_app/` rather than the `/tmp/nccookbook-build` the flag requested. The mismatch between the requested and actual build directory went unnoticed, the report path was then guessed as `_` instead of `_app` (exit 2), and a `find` was needed to recover. The memory has since been corrected and now states the relocation is permanent and names the `_app` path, so the specific trap is closed — the transferable part is that a flag you pass should be confirmed to have taken effect before its output path is used, especially when a remembered workaround may already be obsolete. | project | process | baseline-miss |
| 4 | `grep -rn "…" --include=*.kt app/src/` aborted with `(eval):1: no matches found: --include=*.kt`. The shell is zsh, which glob-expands `*.kt` inside the option value and fails on no match rather than passing it through as bash would. The retry dropped `--include` entirely instead of quoting it, so the filter was lost as well as the round trip. Glob patterns intended for a program rather than the shell must be quoted. | team | tech-stack:zsh | baseline-miss |
