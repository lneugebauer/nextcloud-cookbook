# Task Retro: `String.extractHttpUrl()` URL extraction

**Task spec**: `specs/tasks/155-add-share-recipe-urls-to-import/task-1-extract-http-url.md`
**Sessions analyzed**: 1 (`0f3bffe6`, branch `155-add-share-recipe-urls-to-import`)
**Date**: 2026-08-30

## Session Stats

The session covers task 1 only, start to commit. Commit `31f0295` was authored at 13:25:32Z, three
seconds before the last assistant turn.

| Metric | Value |
|--------|-------|
| Sessions | 1 |
| User messages (iterations) | 2 |
| Errors encountered | 2 counted + 1 masked build failure |
| Duration | 3 min 51 s (13:21:43.934Z → 13:25:35.266Z) |
| Input tokens | 42 |
| Output tokens | 9,762 |
| Cache read tokens | 1,179,153 |
| Cache creation tokens | 40,829 |
| Questions asked (AskUserQuestion) | 0 |
| Subagent spawns (Task) | 0 |
| Tool calls | 19 Bash (0 Edit, 0 Write — both files written as heredocs) |

Error breakdown:

1. **Exit 1** — `cat <memory file>; cat .editorconfig` in one command; there is no repository-root
   `.editorconfig` (it lives at `app/.editorconfig`), so the compound exited non-zero. No impact.
2. **Exit 2** — `ugrep` on `/var/tmp/ncc-build/app/test-results/…`, a guessed path. The relocated
   build layout names the directory `_app`, not `app`. Cost one `find` round trip. See Learning 2.
3. **Uncounted** — `./gradlew test --tests '*StringExtractHttpUrl*'` failed with *"Problem
   configuring task :app:test from command line. > Unknown command-line option '--tests'."*
   `extract-signals.sh` does not see it because the invocation was `2>&1 | tail -30`, so the pipeline
   exited 0. See Learning 1.

## What Went Well

**Two messages, no corrections.** `implement <task spec path>` and, four minutes later, `commit
changes`. Zero clarifications, zero corrections, zero review feedback, zero questions back to the
user.

**The behavioural contract transferred verbatim.** All 21 rows of the §4.1 table became 21 `@Test`
methods, one per row, in the table's own order, with the input and expected value copied unchanged —
including the rows the spec explicitly warned not to condense away (the query string, the
parenthesised URL, the IPv6 pair, the port-terminated host, the scheme-less IP). Confirmed green by
the result XML: `tests="21" skipped="0" failures="0" errors="0"`.

**The spec's prohibitions held.** `android.util.Patterns.WEB_URL` is nowhere in the change — the only
occurrence in the repository is the pre-existing import in `StartScreenViewModel.kt`, untouched. Both
schemes are accepted. No new dependency landed in `app/build.gradle`. The test runs on the plain JVM.

**ktlint passed on the first run.** Before writing a line of Kotlin the session located and read
`app/.editorconfig` (after finding there is no root one), so `./gradlew ktlintCheck` was green
immediately and no formatting round trip was needed.

**The pass was confirmed rather than assumed.** The task spec said to *"confirm it by name rather
than assuming"*. The session did exactly that, parsing `tests=`/`failures=` and the full
`testcase name=` list out of the JUnit XML instead of reading `BUILD SUCCESSFUL`. See Emerged Designs.

**Commit hygiene.** Conventional `feat:` subject, no body, and — unlike all three commits reviewed in
the 208 retro — **no `Co-Authored-By` trailer**. The global instruction beat the harness default this
time. Scope was checked too: the working tree carried an unrelated `fastlane/Fastfile` modification,
which the session diffed, recognised as unrelated, and left out by staging the two new files by path.

**Result: 2 files, +158/−0, no existing file modified**, and no later commit on the branch has
touched either file.

## Spec Accuracy

| Deliverable | Status | Notes |
|-------------|--------|-------|
| `StringExtractHttpUrlExtension.kt` at the prescribed path, package `core.util` | as-specified | 25 lines, new file |
| Single top-level `fun String.extractHttpUrl(): String?`, no class/object | as-specified | Matches the `StringAddSuffixExtension.kt` shape |
| First `http://`/`https://` token, case-insensitive, whitespace-terminated | as-specified | `Regex("""https?://\S+""", RegexOption.IGNORE_CASE)` |
| Regex as a private top-level `val`, compiled once | as-specified | |
| Strip `. , ; : ! ? > " '` unconditionally | as-specified | Hoisted into a `private const val TRAILING_PUNCTUATION` |
| Strip `)` `]` `}` only while unbalanced | as-specified | `private val CLOSING_BRACKETS` map plus a `count` comparison per candidate |
| Return `null` on no match or empty remainder after the scheme | as-specified | `takeIf { it.substringAfter("://").isNotEmpty() }` — but see Under-Design: no test reaches this guard |
| Return the match verbatim, no normalisation | as-specified | `HTTPS://EXAMPLE.COM/R` round-trips |
| No host or port validation | as-specified | Nothing parses the authority |
| No `android.util.Patterns` | as-specified | Verified across `app/src/main` and `app/src/test` |
| Both `http` and `https` | as-specified | |
| No KDoc unless genuinely non-obvious | as-specified | Zero comments in the file |
| Test at `app/src/test/.../StringExtractHttpUrlUnitTest.kt`, root test package | as-specified | Not mirroring `core/util/`, exactly as the spec insisted |
| JUnit 4, no mocks, `assertEquals`, `string_DoesSomething_ReturnsSomething()` naming | as-specified | Matches `StringAddSuffixUnitTest`, not the backtick style of `DownloadRecipeViewModelUnitTest` |
| All 21 table rows as unit tests | as-specified | 21 `@Test` methods; XML confirms 21 executed, 0 failed |
| Two extra private top-level constants (`TRAILING_PUNCTUATION`, `CLOSING_BRACKETS`) | added | Not prescribed; a reasonable elaboration of the "compile it once" instruction, and it keeps the function body readable |
| Verification `./gradlew ktlintCheck test` | modified | Both green — but only after the spec's own `--tests` suggestion was rejected by Gradle and rerun as `testFullDebugUnitTest --tests` |
| Verification "check `app/build/reports/tests/`" | modified | Reports do not land there in this checkout; found under `/var/tmp/ncc-build/_app/test-results/` after a `find` |
| Commit | added | The task spec has no `## Commit` section; the user asked for it in a second message |

Nothing in the task spec was dropped.

## Spec Gaps

**None on behaviour.** The 21-row table is a complete contract and was implementable without a single
question — the spec even pre-answered the two traps it knew about (why the `)` of
`(https://example.com/recipe)` must go while the `]` of `http://[fd00::1]` must stay; why the `:` of
`:8080` needs no special handling and must not get any). Neither trap produced a wrong first draft.

**One process gap.** The task spec has no `## Commit` section, and neither do tasks 2 and 3 of this
breakdown. Every task spec in `183-fix-passkey-login` and `208-fix-copy-single-ingredient-copies-default-amount`
has one. The consequence here was small — the user sent `commit changes` as a second message — but it
is a silent regression in the breakdown template rather than a deliberate choice. See Learning 3.

The spec correctly kept the local eCryptfs/build-directory workaround out of the committed file, per
`keep-local-setup-out-of-specs`. The friction that caused instead came from agent memory, not from
the spec — see Learning 2.

## Over-Design

**Nothing attributable to the spec.** The bracket-balancing rule reads like gold-plating until you
notice that two required inputs disagree about the same character, and the spec argues that case
explicitly in both §3.2 and the task file. It earned its place.

One dead condition in the implementation: the strip loop is guarded by `while (url.isNotEmpty())`,
but the match always begins with `http`, and `/` is not in the strip set — so the loop breaks at the
second `/` at the latest and the string can never be emptied. The guard can never fire. Harmless, one
line, not worth a learning.

## Under-Design

**The `"https://"` row does not test the rule it was written to prove.** The spec states two distinct
`null` conditions — *"when nothing matches, **or** when the remainder after the scheme is empty"* —
and offers `"https://"` as the row covering the second. It does not. `\S+` requires at least one
character after `://`, so `"https://"` produces no match at all and returns at the `?: return null`.
The `takeIf { it.substringAfter("://").isNotEmpty() }` guard is therefore never reached by any of the
21 rows, and deleting it would leave the whole suite green.

The guard is not redundant — it fires for inputs where stripping consumes the entire remainder, e.g.
`"Look at https://."` matches `https://.`, strips the dot, and would otherwise return the useless
`"https://"`. That is a plausible shared payload. It is simply untested, because the spec reasoned
about the two rules separately and never checked whether the row assigned to the second one gets past
the first. Verified against the shipped regex and strip logic while writing this retro. See Learning 4.

## Code Review & Corrections

### User Message Classification

| # | Timestamp | Intent | Summary |
|---|-----------|--------|---------|
| 1 | 2026-08-30T13:21:47Z | initial-prompt | `implement specs/tasks/155-add-share-recipe-urls-to-import/task-1-extract-http-url.md` |
| 2 | 2026-08-30T13:25:16Z | instruction | `commit changes` |

Zero corrections, zero clarifications, zero review feedback.

### Review Findings

| # | Finding | Root Cause | Scope | Domain |
|---|---------|-----------|-------|--------|
| 1 | `./gradlew test --tests '<pattern>'` is invalid — `test` is a lifecycle task and takes no `--tests` | spec-wrong | project | tech-stack:gradle |
| 2 | A redundant build-dir init script was written from a stale memory note, and the report path was guessed wrong | baseline-miss | project | process |
| 3 | No `## Commit` section in the task spec, unlike every 183/208 task spec | spec-gap | project | process |
| 4 | The `"https://"` row cannot reach the empty-remainder guard it was chosen to cover | under-design | team | process |

## Emerged Designs

**Asserting on the JUnit result XML instead of on `BUILD SUCCESSFUL`.** To satisfy the spec's
*"confirm it by name rather than assuming"*, the session grepped
`tests="21" skipped="0" failures="0" errors="0"` and the full `testcase name=` list straight out of
`TEST-…StringExtractHttpUrlUnitTest.xml`. This proves three things a green build does not: the class
ran at all, every case ran, and none was silently skipped. It pairs well with the mutation check that
emerged in the 208 retro — that one proves an assertion is evaluated, this one proves the test is
executed — and both belong in the spec template's verification guidance.

**Reading `app/.editorconfig` before writing Kotlin.** Cheap, and it bought a first-try `ktlintCheck`.
Worth making a habit in this repository, where the config is on the module and not at the root.

**Hoisting the punctuation set and the bracket map to private top-level constants.** The spec asked
only that the `Regex` be hoisted. Extending that to `TRAILING_PUNCTUATION` and `CLOSING_BRACKETS`
turns the strip loop into a readable predicate and puts the two rules where a reader looks first —
a better answer than the spec's, in a file the spec insisted must carry no KDoc.

## Learnings

| # | Learning | Scope | Domain | Root Cause |
|---|----------|-------|--------|------------|
| 1 | The task spec's Verification block suggested confirming the new class *"by running with `--tests '*StringExtractHttpUrl*'`"*, applied to the `./gradlew … test` command it had just prescribed. Gradle rejected it: `test` is a lifecycle task and accepts no `--tests` option; only a concrete `Test` task such as `testFullDebugUnitTest` does. This is the second flavour-related wrong Gradle command a spec has prescribed in this repository — the 208 retro logged `testDebugUnitTest` for the same underlying reason. `CLAUDE.md` still lists `testDebugUnitTest` under Testing, so specs keep inheriting bad commands from it; fixing that file is the single change that stops both. | project | tech-stack:gradle | spec-wrong |
| 2 | The session read the `kapt-fails-on-ecryptfs-home` memory, which still described the build-directory relocation as a manual `--init-script` workaround, and duly wrote one into the scratchpad — then never passed it, because a permanent `~/.gradle/init.d/` script was already installed and plain `./gradlew` worked. It then guessed the relocated report path as `app/test-results` when the layout actually names it `_app`, and needed a `find` to recover. Both cost round trips, and both would have been avoided by running the build once and looking at where output landed before acting on a remembered workaround. (The memory has since been corrected by a later session and now records both facts.) | project | process | baseline-miss |
| 3 | The task spec has no `## Commit` section, so the user had to send `commit changes` as a second message. All five task specs in the two preceding breakdowns (183, 208) have one, and none of the three in this breakdown does — a silent regression in the task-breakdown template rather than a deliberate choice. | project | process | spec-gap |
| 4 | The spec specified two `null` conditions — no match, and an empty remainder after the scheme — and assigned `"https://"` to prove the second. It cannot: the mandated `\S+` in the regex rejects that input before the remainder check runs, so the shipped guard has no covering test and could be deleted with the suite still green. The spec analysed each rule correctly in isolation and never checked whether the row chosen for one is reachable past the other. When a spec fixes both the implementation technique and the test table, it has to trace at least one input per rule through the technique it prescribed. | team | process | under-design |
