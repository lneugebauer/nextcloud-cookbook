# Tasks — Fix: copying a single ingredient copies the default amount

Spec: [`specs/spec/208-fix-copy-single-ingredient-copies-default-amount.md`](../../spec/208-fix-copy-single-ingredient-copies-default-amount.md)
Issue: [#208](https://github.com/lneugebauer/nextcloud-cookbook/issues/208)

## Approach

The bug is a stale capture: the ingredient long-press listener is registered inside
`LaunchedEffect(Unit)` at `RecipeDetailScreen.kt:720`, so it holds the `String` captured during the
first composition of that slot and never sees a recalculated yield.

The fix is one vertical slice (task 2): a `getIngredientAt(index)` lookup on the ViewModel resolved
against live state, threaded down through `RecipeDetailLayout` → `Ingredients`, plus a
`BindLongClick` helper that registers via `DisposableEffect` + `rememberUpdatedState` so a stale
capture becomes impossible by construction.

Task 1 comes first only because task 2's regression tests need a `RecipeDetailViewModel` unit-test
harness that does not exist yet — no ViewModel in this project has a test. Task 1 stands that harness
up and, while it is there, locks in the whole-recipe share path (`getShareText()`), which §2.4
confirmed is already correct and wants guarded.

## Tasks, in dependency order

1. **[ViewModel test harness + share-path regression guard](task-1-viewmodel-test-harness-share-guard.md)**
   Adds `kotlinx-coroutines-test`, creates `RecipeDetailViewModelUnitTest.kt` with the
   `Dispatchers.setMain(UnconfinedTestDispatcher())` fixture, and asserts that `getShareText()`
   substitutes recalculated ingredients and the current yield (spec §4 cases 5–6).
   *No production code changes.* Depends on nothing.

2. **[Fix stale ingredient copy, end to end](task-2-fix-stale-ingredient-copy.md)**
   `getIngredientAt` on the ViewModel, `BindLongClick` replacing the `LaunchedEffect(Unit)` block,
   the parameter threaded through both composables and both previews, `ScreenshotsTestSuite`
   updated, and spec §4 cases 1–4 appended to the test file from task 1.
   Depends on task 1 (same test file, same dispatcher fixture, same dependency).

## Verification notes that apply to both tasks

- CI runs `bundle exec fastlane build` (`.github/workflows/ci.yml:32`), which is
  `clean → ktlintCheck → lint → test → assemble` (`fastlane/Fastfile:111`). `ktlintCheck` runs
  **before** `test`, so a formatting slip fails the build before any test runs — run
  `./gradlew ktlintFormat` before pushing.
- `assemble` builds `FullRelease`, so the `@Preview` composables in the main source set **are**
  compile-checked by CI.
- **androidTest is never compiled by CI.** A forgotten `ScreenshotsTestSuite.kt` update passes CI
  and only breaks the `screenshots` lane later. Task 2 must verify with
  `./gradlew compileFullDebugAndroidTestKotlin` locally.
- `connectedAndroidTest` does not run in CI either, which is why the Compose binding itself is
  covered by the manual steps in spec §4 rather than by an instrumented test.

## Out of scope for both tasks

Per spec §2.1b: no move off twain, no owned `AndroidView` markdown composable, no removal of the
`generateViewId` + `findViewById` mechanism. No `CHANGELOG.md` (none exists — release notes derive
from commit messages) and no edits to `fastlane/metadata/android/*/changelogs/*.txt`.
