# Task Retro: Share target registration and gated navigation

**Task spec**: `specs/tasks/155-add-share-recipe-urls-to-import/task-3-share-target-intent-handling.md`
**Sessions analyzed**: 1 (`3d8dbcf3`, branch `155-add-share-recipe-urls-to-import`)
**Date**: 2026-08-30

## Session Stats

The session covers task 3 start to finish, and continues past it into the commit, the push and the
pull request for the whole branch. The commit was authored in-session as `c0fab70` and appears on the
branch today as `0c23bbd` after a later rebase; the content is unchanged. PR
[#211](https://github.com/lneugebauer/nextcloud-cookbook/pull/211) covers all three task commits.

| Metric | Value |
|--------|-------|
| Sessions | 1 |
| User messages (iterations) | 4 |
| Errors encountered | 1 |
| Duration (wall clock) | 23 min 12 s (14:00:47.778Z → 14:23:59.463Z) |
| Duration (agent working) | ~14 min 30 s across four bursts; ~8 min 40 s of the wall clock is the user thinking between them |
| Input tokens | 166 |
| Output tokens | 41,839 |
| Cache read tokens | 8,636,882 |
| Cache creation tokens | 132,846 |
| Questions asked (AskUserQuestion) | 0 |
| Subagent spawns (Task) | 0 |
| Tool calls | 62 Bash, 17 Read (0 Edit, 0 Write — auto mode, so all three files were edited through Bash heredocs) |

**Implementation was about a minute; verification was ten.** All three files were written between
14:01:35 and 14:02:04. The next nine minutes are `ktlintCheck`, `test`, `lint`,
`compileFullDebugAndroidTestKotlin`, an install, and eight of the ten manual cases of §4.3 driven on
a live emulator against the user's real Nextcloud server. That ratio is the opposite of tasks 1 and
2, where the agent implemented and the user did the device work.

The single error was exit 2 from a compound status command whose last statement was
`ls .github/pull_request_template.md .github/PULL_REQUEST_TEMPLATE.md 2>/dev/null` — `ls` exits 2 when
no file matches, which is the intended "no template" answer. Nothing was retried and nothing was lost.
No error touched the code under change.

## What Went Well

**Four messages, zero corrections, zero questions, zero rework.** `implement <task spec path>`, then a
URL the agent had asked for, then `Leave task 3. Commit changes.`, then `push and open a pr`. Not one
file was edited twice, and not one line of the implementation was revised after it was written.

**The implementation matches the task doc point for point.** Every prescription landed as written:
`singleTop` and the unlabelled `ACTION_SEND` filter, `getCharSequenceExtra` rather than
`getStringExtra`, the `savedInstanceState == null` guard, the four-condition gate including the
load-bearing `currentDestination == null` half, `popUpTo(DownloadRecipeScreenDestination) { inclusive = true }`,
and clearing the state only *after* a successful navigation. There is nothing in the diff the spec did
not ask for.

**The spec's one explicit trap was avoided.** The task doc warned that naming the outer local
`destinationsNavigator` would shadow the `DestinationScope` receiver property used inside the splash
`composable` block, and suggested a distinct name. The session named it `appNavigator`
(`MainActivity.kt:146`) and never had to check whether the splash screen still navigated.

**It ran the build check its own task doc had dropped.** Task 3's Verification block lists only
`./gradlew ktlintCheck test lint`. The session went back to the parent spec's §4.4, found the
`compileFullDebugAndroidTestKotlin` requirement there, and ran it — correctly, since task 3 changes
the `NextcloudCookbookApp` signature and CI never compiles the `androidTest` source set. This is the
second practice in this breakdown to carry forward on its own; task 2 did the same with task 1's
JUnit-XML parsing.

**It drove the manual verification itself instead of handing it back.** Tasks 1 and 2 ended with the
agent stating it could not do the device work; here it found `adb`, installed the debug build,
confirmed the filter was registered with `dumpsys`, and walked cases 1, 2, 4, 5, 6, 7, 8 and 10 —
sending intents, taking `screencap` screenshots, and reading them back. Two details raise this above
"it looked right":

- For `singleTop` (case 2) the evidence is the framework's own verdict, not a screenshot:
  `am start` reporting *"delivered to currently running top-most instance"*, the same `ActivityRecord`
  and zero `onDestroy` in logcat.
- For back-navigation it compared the post-back screenshot against a screenshot of the destination
  taken beforehand and reported them **pixel-identical**, rather than eyeballing "this looks like the
  recipe list".

**It said plainly what it had not done.** The 14:11 summary named the two unrun cases and why —
case 3 needs credentials it does not have, case 9 needs a recipe already in the live library — offered
to run case 9 given a URL, noted that the `Spanned` criterion is unverifiable through `adb` at all, and
closed with *"I haven't committed."* When the user supplied the URL, case 9 ran immediately.

**A pre-existing bug was identified as pre-existing, with proof rather than assertion.** See
Emerged Designs.

**A stale project memory was corrected mid-session.** At 14:02 the session followed the
`kapt-fails-on-ecryptfs-home` memory and wrote a build-directory relocation init script. At 14:11 it
checked whether a permanent relocation already existed, found
`~/.gradle/init.d/nextcloud-cookbook-builddir.gradle`, and rewrote the memory to say the workaround is
obsolete and where reports actually land. Tasks 1 and 2 both walked into this and both left it in
place; this session closed it.

## Spec Accuracy

| Deliverable | Status | Notes |
|-------------|--------|-------|
| `android:launchMode="singleTop"` on `MainActivity` | as-specified | `AndroidManifest.xml:33` |
| Third `<intent-filter>`: `SEND` / `DEFAULT` / `text/plain`, no `android:label` | as-specified | `AndroidManifest.xml:48-52`; share sheet shows "Cookbook" with the app icon, confirmed in case 10 |
| `_sharedTextState` / `sharedTextState` pair | as-specified | `MainViewModel.kt:34-35`, placed directly below the existing `_intentState` pair as directed |
| `setIntent()` branch: `ACTION_SEND` + `text/plain`, `getCharSequenceExtra`, trim, publish if non-blank | as-specified | `MainViewModel.kt:48-55`; `_intentState` left untouched |
| `onSharedTextHandled()` | as-specified | `MainViewModel.kt:58-60` |
| `onCreate` guarded by `savedInstanceState == null`; `onNewIntent` unconditional | as-specified | `MainActivity.kt:81-83` |
| Collect `sharedTextState` in `setContent`, pass it with `viewModel::onSharedTextHandled` | as-specified | `MainActivity.kt:89`, `MainActivity.kt:116-120` |
| `NextcloudCookbookApp(intent, sharedText, onSharedTextHandled)` | as-specified | `MainActivity.kt:137-141` |
| `appNavigator` + `currentDestination` locals | as-specified | `MainActivity.kt:146-147`; took the spec's naming caution and used a distinct name |
| Gated `LaunchedEffect` with all four conditions | modified | `MainActivity.kt:214-227`. `LocalCredentials.current` is hoisted into a `credentials` local (`MainActivity.kt:148`) and that local is the effect key, rather than `LocalCredentials.current` appearing inline in the key list. Behaviourally identical, and it keeps the key list readable. |
| Navigate with `popUpTo(DownloadRecipeScreenDestination) { inclusive = true }`, then clear | as-specified | Clearing strictly after the navigate call is what makes requirement 6 work with no logged-out branch |
| No `ACTION_SEND_MULTIPLE`, `EXTRA_STREAM`, `ACTION_VIEW`, no new string resource | as-specified | None present in the diff |
| `./gradlew ktlintCheck test lint` | as-specified | All three green |
| `compileFullDebugAndroidTestKotlin` | added | Not in task 3's Verification block; taken from parent spec §4.4 |
| §4.3 manual walkthrough, ten cases | modified | Eight run in the first pass (1, 2, 4, 5, 6, 7, 8, 10); case 9 run after the user supplied a URL; **case 3 (logged out) never run** |
| Explanatory comments on the two non-obvious guards | added | The `savedInstanceState` guard and the `currentDestination == null` check each carry a comment stating why they exist, so the spec's reasoning survives in the code |

Acceptance criteria: five of seven are verified on a device. **"Sharing while logged out holds the URL
and imports after the login flow completes"** and **"Styled text (a `Spanned` payload) is not silently
dropped"** rest on code inspection only — see Spec Gaps.

## Spec Gaps

**Case 3 was declared mandatory with no route to running it.** The task doc's Verification block
singles out case 3 as one of four that *"must not be skipped"*, but the only tool it supplies is the
`adb am start` line, and the case needs the emulator signed *out* of the user's Nextcloud account and
then signed back *in*. The session stopped at exactly that point — *"I don't have the credentials to
sign back in"* — which is the right call, and the criterion is simply unconfirmed. A mock-Nextcloud
sign-in harness had already been built in this repository during the #183 task-2 work; the memory
recording how to run it (`emulator-signin-harness-needs-adb-reverse`) was only written at 18:08 the
same day, four hours after this session, so it was not available here.

**The `Spanned` acceptance criterion has no verification path at all.** The spec argues at length for
`getCharSequenceExtra` over `getStringExtra` and then lists *"Styled text is not silently dropped"* as
an acceptance criterion — but `adb --es` can only put a `String` in `EXTRA_TEXT`, so the prescribed
verification cannot distinguish the two calls. The session noticed and said so. Nothing in the spec
would have caught a regression here; a `Spanned` payload needs either a second app that sends one or a
Robolectric test the spec explicitly ruled out (§4.3).

**No `## Commit` section — third occurrence in this breakdown.** The user had to send
`Leave task 3. Commit changes.` as a separate message, exactly as in tasks 1 and 2. All five task specs
in the preceding 183 and 208 breakdowns have one; none of the three in 155 does.

**`compileFullDebugAndroidTestKotlin` was dropped from task 3's Verification block** although parent
spec §4.4 requires it and task 2's own task doc carried it. Task 3 is the task that changes a
composable signature, so it is the one where the check matters most. No cost was incurred — the
session read §4.4 itself — but the omission was real.

## Over-Design

None found. The task doc is unusually prescriptive — it dictates local variable names, the exact order
of two statements, and which of two `Intent` accessors to call — and every one of those prescriptions
earned its place. The `getCharSequenceExtra` insistence prevents a silent data loss; the naming caution
prevents a shadowing bug; the "clear only after navigating" ordering removes an entire logged-out code
path. The `currentDestination == null` gate is the clearest case: the spec spends a paragraph
justifying a check that looks defensive and is not, and that paragraph is why the shipped code has it
and a comment explaining it.

## Under-Design

**§4.3 case 9 asserts an outcome the code cannot produce.** The case reads: *"the automatic import
gets an HTTP 409 and the conflict snackbar appears with the recipe name; View original opens it"*.
Run against the user's live library, the 409 arrives and `ConflictState.Active` is set exactly as
§2.10 describes — but the snackbar renders the generic `409 Conflict: Recipe already exists` with no
recipe name and no *View original* action.

The cause is one argument two layers down: `recipe/data/repository/RecipeRepositoryImpl.kt:237` calls
`handle409ConflictError(response, name = "")` on the import path. A blank name skips the previews
lookup, so `existingRecipe` is null, the DTO comes out as `RecipeConflictDto(id = null, name = "")`,
`toUiText()` takes its blank-name branch, and `ConflictSnackbar` drops the action label because the id
is null. It arrived with `f54ddfd` (PR #207) and sits below all three 155 commits.

§2.10's reasoning is correct *at the ViewModel level* — the ViewModel does translate the 409 into a
conflict state and reset to `Initial(url)`, and the session confirmed both. What the spec never did was
follow the value that populates the snackbar back to the repository call that supplies it. It reasoned
about the layer it was changing and assumed the layer beneath it was already right.

**This is the third under-design of the same shape in this breakdown.** Task 1's spec chose `"https://"`
to cover the empty-remainder guard, and the regex it also prescribed rejects that input before the guard
runs. Task 2's spec required skipping a *"null or blank"* `sharedText` and derived a test row only for
the null half. Here it asserts an end-state without tracing the call chain that produces it. In all
three the spec states an expectation and never walks it through the code that has to satisfy it.

## Code Review & Corrections

### User Message Classification

| # | Timestamp | Intent | Summary |
|---|-----------|--------|---------|
| 1 | 14:01:06.499Z | initial-prompt | `implement specs/tasks/155-add-share-recipe-urls-to-import/task-3-share-target-intent-handling.md` |
| 2 | 14:15:52.673Z | clarification | Supplies the HelloFresh URL the agent had asked for, to make case 9 runnable: *"That is in my library and should create a conflict."* |
| 3 | 14:21:08.451Z | instruction | *"Leave task 3. Commit changes."* — answers the agent's scope question by declining the 409-snackbar fix, and asks for the commit |
| 4 | 14:22:33.948Z | instruction | *"push and open a pr"* |

Two of four messages are the user unblocking work the agent could not do alone (a URL from the live
library, a scope decision). **No corrections and no review feedback** — nothing the agent wrote was
sent back for a change.

### Review Findings

| # | Finding | Root Cause | Scope | Domain |
|---|---------|-----------|-------|--------|
| 1 | §4.3 case 9's expected outcome ("snackbar with the recipe name; *View original* opens it") does not hold; the import path passes `name = ""` at `recipe/data/repository/RecipeRepositoryImpl.kt:237` so the generic 409 string renders with no action | under-design | project | process |
| 2 | Two acceptance criteria (logged-out hold, `Spanned` payload) have no verification path in the spec and remain unconfirmed | spec-gap | project | process |
| 3 | Third task spec in this breakdown with no `## Commit` section | spec-gap | project | process |

## Emerged Designs

**A regression-vs-pre-existing protocol, applied without being asked.** When the conflict snackbar came
out wrong, the session did not guess and did not report it as a task-3 defect. It: (a) re-ran the same
import through the *pre-existing* manual path — typing the URL and tapping Download — and got a
pixel-identical snackbar, proving the automatic path behaves exactly like the button; (b) read the
conflict chain from `importRecipe()` down to `ConflictSnackbar` to name the responsible line; (c)
`git blame`d that line to `f54ddfd` and confirmed with `merge-base` that the commit sits below all
three 155 commits. Only then did it report — with a diagnosis, a reason it is not a one-liner (neither
`RecipePreview` nor `RecipePreviewDto` carries a source URL, so the existing recipe cannot be matched
from a URL alone), a lead worth pursuing (the 409 branch discards the server's own `msg`, which the
non-409 branch does show), and a direct question about scope. The user answered in six words. That
sequence — reproduce through the old path, locate, blame, then ask — is worth reusing whenever a manual
walkthrough turns up something ugly.

**Autonomous device verification as the default, not the fallback.** Tasks 1 and 2 both ended with the
agent naming the manual check as the one thing it could not do. Task 3 did it: `adb install`,
`dumpsys package` to confirm the filter, `am start` for each case, `screencap`/`adb pull`, then reading
the PNGs back. Eight cases in roughly eight minutes, against the real server. The two evidence habits
that make the results trustworthy — using the framework's own logcat verdict for `singleTop` rather
than a screenshot, and asserting **pixel-identical** screenshots for back-navigation rather than "looks
right" — are the transferable part.

**Load-bearing invariants documented at the point of use.** Both non-obvious guards carry a one- or
two-line comment saying what breaks without them (`MainActivity.kt:79-80`, `MainActivity.kt:216-218`).
The spec argued both at length; without the comments that reasoning would live only in
`specs/spec/155-…` and the next person to tidy up a "redundant null check" would delete the fix.

**Known gaps written into the PR body rather than omitted.** The PR records the 409-snackbar gap and
the manual-verification results, including what was not run.

## Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | §4.3 case 9 promised a conflict snackbar "with the recipe name; *View original* opens it". The 409 fires and the ViewModel behaves exactly as §2.10 says, but the snackbar is the generic string with no action, because `recipe/data/repository/RecipeRepositoryImpl.kt:237` passes `name = ""` on the import path and a blank name skips the previews lookup that would supply the id. §2.10 reasoned correctly about the layer it was changing and assumed the layer beneath it already worked. **This is the third occurrence of one shape in this breakdown**: task 1 chose `"https://"` for a guard its own prescribed regex makes unreachable, task 2 stated a "null or blank" rule and derived a row for null only, and task 3 asserts an end-state without tracing the call chain that produces it. A spec that fixes both an expectation and the code path must walk at least one input end to end through the path it prescribed — including the layers it is *not* changing. | team | process | under-design |
| 2 | Two of the seven acceptance criteria — the logged-out hold and the `Spanned` payload — cannot be checked by any method the spec supplies. Case 3 is marked "must not be skipped" but needs the emulator signed out of and back into a real Nextcloud account, and the spec offers only the `adb am start` line; `adb --es` can only put a `String` in `EXTRA_TEXT`, so the `getCharSequenceExtra`-vs-`getStringExtra` distinction the spec argues for at length is invisible to it. The agent ran everything it could and said clearly what it could not, so nothing was faked — but both criteria shipped unconfirmed, and a regression in either would be silent. An acceptance criterion needs a stated way to reach it, or an explicit note that it is code-review-only. (A mock-Nextcloud sign-in harness from the #183 work would have covered case 3; the memory recording it was written four hours after this session.) | team | process | spec-gap |
| 3 | The task spec has no `## Commit` section, so the user had to fold the commit into a separate message. Third occurrence in this breakdown after tasks 1 and 2, against five for five in the 183 and 208 breakdowns — the task-breakdown template regressed for spec 155 as a whole, not task by task. | project | process | spec-gap |
| 4 | The session again read the `kapt-fails-on-ecryptfs-home` memory, wrote the build-directory relocation init script it described, and passed `-DrelocatedBuildDir=/tmp/nc-cookbook-build` — which had no effect, because the permanent `~/.gradle/init.d/` script wins. Third session in a row to build the obsolete workaround before checking whether the problem was still live. It cost nothing this time only because no report path was needed. Nine minutes later the session did check, found the permanent script, and corrected the memory, so this specific trap is now closed; the transferable part is that a remembered workaround should be tested for still being necessary before it is re-erected. | project | process | baseline-miss |
| 5 | Task 3's Verification block lists only `./gradlew ktlintCheck test lint`, dropping the `compileFullDebugAndroidTestKotlin` check that parent spec §4.4 requires and task 2's task doc carried — and task 3 is the task that changes a composable signature, so it is where an `androidTest` break was most likely. The agent went back to §4.4 and ran it anyway, so nothing broke, but a task doc should not silently narrow a build check the parent spec mandates. | project | process | spec-gap |
