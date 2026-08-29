# Task 2 — Fix stale ingredient copy, end to end

**Spec:** `specs/spec/208-fix-copy-single-ingredient-copies-default-amount.md` — read §2.1a, §2.1b,
§2.3, §3.2, §3.3, §3.4, §3.5, and §4 (cases 1–4 plus the manual verification steps).
**Issue:** [#208 — Copying single ingredient always uses default amount](https://github.com/lneugebauer/nextcloud-cookbook/issues/208)

**Dependencies:** [`task-1-viewmodel-test-harness-share-guard.md`](task-1-viewmodel-test-harness-share-guard.md)
must be merged first. It adds the `kotlinx-coroutines-test` dependency and creates
`app/src/test/java/de/lukasneugebauer/nextcloudcookbook/recipe/presentation/detail/RecipeDetailViewModelUnitTest.kt`
with the `Dispatchers.setMain(UnconfinedTestDispatcher())` fixture, the `RECIPE` fixture, and test
cases 5 and 6. **Append** cases 1–4 to that existing file; do not create a second test file and do
not rewrite the fixture.

## Goal

Long-pressing an ingredient on the recipe detail screen must copy the **currently displayed**
(recalculated) text, not the amount captured at first composition.

Root cause, already verified in the spec: `RecipeDetailScreen.kt:720` registers the long-click
listener inside `LaunchedEffect(Unit)`. The key is `Unit`, so the effect never restarts and the
lambda holds the `ingredient` `String` from the first composition of that slot. `MarkdownText`
(twain 0.3.2) builds its `TextView` in the `AndroidView` **factory** block and its `update` block
only re-renders the markdown, so the listener — and its captured string — survives every
recomposition.

## What to implement

### 1. `RecipeDetailViewModel.kt`

`app/src/main/java/de/lukasneugebauer/nextcloudcookbook/recipe/presentation/detail/RecipeDetailViewModel.kt`

Add a public lookup beside `getShareText()`, matching that method's getter naming style:

```kotlin
fun getIngredientAt(index: Int): String =
    _state.value.calculatedIngredients.getOrNull(index)?.ingredient
        ?: _state.value.data?.ingredients?.getOrNull(index)?.value
        ?: ""
```

The `data.ingredients` fallback mirrors the existing `calculatedIngredients.ifEmpty { ... }` at
`RecipeDetailScreen.kt:408`, which keeps `RecipeDetailLayout` renderable with
`calculatedIngredients = emptyList()` (used by the previews and `ScreenshotsTestSuite`). In
production both fields are assigned in the same `_state` update, so the fallback is defensive only.

Index lookup is safe: `YieldCalculatorImpl.recalculateIngredients` is a plain `ingredients.map { }`,
so size and order are preserved across every recalculation.

### 2. `RecipeDetailScreen.kt` — the `BindLongClick` helper

`app/src/main/java/de/lukasneugebauer/nextcloudcookbook/recipe/presentation/detail/RecipeDetailScreen.kt`

Add a private composable. Place it near `KeepScreenOn` (`:215`), which is the same shape — a private
composable that emits no UI and exists only to wrap a `DisposableEffect` keyed to a `Context`
lookup:

```kotlin
@Composable
private fun BindLongClick(
    viewId: Int,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    DisposableEffect(viewId) {
        val view = context.getActivity()?.findViewById<View>(viewId)
        view?.setOnLongClickListener {
            currentOnLongClick()
            true
        }
        onDispose { view?.setOnLongClickListener(null) }
    }
}
```

Write the listener body across two lines exactly as shown — **not** `{ currentOnLongClick(); true }`.
`ktlintCheck` runs before `test` in the `build` lane and the semicolon form is not worth the risk.
`app/.editorconfig` already exempts `@Composable` functions from ktlint's function-naming rule, so
the capitalised name is fine.

This removes three defects at once:
1. `rememberUpdatedState` lives inside the helper, so it always invokes the newest lambda —
   staleness becomes impossible by construction. **This, not the ViewModel lookup, is what fixes #208.**
2. The listener is now removed on dispose; the current code never clears it.
3. No coroutine is launched for a synchronous registration.

Key on `viewId`, not on the ingredient — `findViewById` then runs once per view rather than per
recomposition. `LaunchedEffect(ingredient)` was explicitly rejected as the fix.

**Do not** replace `findViewById` with a Compose `combinedClickable` wrapper. Markwon's
`CorePlugin.afterSetText` installs a `LinkMovementMethod` when none is set, so the `TextView`
consumes touch events — and ingredient values really can contain links, because
`RecipeDetailViewModel.enrichRecipeLinks` rewrites `#r/123` into markdown links inside
`newIngredients`. Owning the `AndroidView` is the structural fix and is deliberately out of scope
(spec §2.1b).

**Effect timing is safe.** Swapping `LaunchedEffect` for `DisposableEffect` moves `findViewById`
from a dispatched coroutine to a synchronous call during change application. The spec traced
`Composition.applyChangesInLocked` (runtime 1.10.0): all node changes and `applier.onEndChanges()`
run *before* `rememberManager.dispatchRememberObservers()`, so the interop `View` is already
attached to the decor tree. If a long-press stops working during manual testing, this is the first
thing to re-check — but it was verified, not assumed.

### 3. `RecipeDetailScreen.kt` — wiring

| Location | Change |
| --- | --- |
| `RecipeDetailLayout` signature (`:329`) | Add `getIngredientAt: (index: Int) -> String` |
| `RecipeDetailLayout` call in `RecipeDetailScreen` (`:162`) | Pass `getIngredientAt = viewModel::getIngredientAt` |
| `Ingredients` call (`:407`) | Forward `getIngredientAt` — **see the positional-args note below** |
| `Ingredients` signature (`:585`) | Add `getIngredientAt: (index: Int) -> String` |
| Ingredient loop (`:678`) | `ingredients.forEach { (ingredient, hasCorrectSyntax) -> }` → `ingredients.forEachIndexed { index, (ingredient, hasCorrectSyntax) -> }` |
| Long-click block (`:720`–`:748`) | Replace the entire `LaunchedEffect(Unit)` block with the `BindLongClick` call below |
| `IngredientsPreview` (`:961`) | Pass `getIngredientAt = { "" }` |
| `RecipeDetailLayoutPreview` (`:1013`) | Pass `getIngredientAt = { "" }` |

**Positional-args note:** the `Ingredients(...)` call at `:407` passes its arguments **positionally**,
unlike the previews which use named arguments. Either add the new argument in the correct position
or convert that call to named arguments — do not assume you can append it.

The call site replacing `:720`–`:748`:

```kotlin
BindLongClick(viewId = textViewId) {
    val ingredientToCopy = getIngredientAt(index)
    if (ingredientToCopy.isNotBlank()) {
        scope.launch {
            clipboard.setClipEntry(
                ClipEntry(ClipData.newPlainText("ingredient", ingredientToCopy)),
            )
            // existing SDK_INT <= S_V2 toast, unchanged
        }
    }
}
```

The gesture is still consumed on a blank result (`BindLongClick` always returns `true`); only the
clipboard write and the toast are skipped.

**`context` stays in `Ingredients`.** The spec's prose says "`context` and `LocalContext` move into
the helper" — that is true of the *long-click registration*, but `Ingredients` still needs
`val context = LocalContext.current` (`:595`) for the bulk-copy toast at `:625`–`:626`
(`Toast.makeText(context, context.resources.getQuantityString(...))`) and for the per-ingredient
toast inside the new `BindLongClick` lambda. `BindLongClick` reads its own `LocalContext.current`.
Do not delete `context` from `Ingredients`. `scope` (`rememberCoroutineScope()`) and `clipboard`
(`LocalClipboard`) also stay — `setClipEntry` is `suspend`.

Everything else in `Ingredients` is unchanged: the checkbox, `MarkdownText`, the syntax indicator,
and the bulk copy button. `ingredient` is still used for rendering. The bulk copy button at `:610`
is already correct — its `onClick` is a composable parameter, so it re-captures on every
recomposition. Do not touch it.

**Imports**, checked line by line against the current file:
- **Add** `androidx.compose.runtime.rememberUpdatedState` — the only genuinely new import.
- **Already present, leave alone:** `androidx.compose.runtime.DisposableEffect` (`:64`),
  `androidx.compose.runtime.getValue` (`:68`, needed for the `by` delegate), `android.view.View`
  (`:9`, used by `View.generateViewId()` at `:688`).
- **Keep** `androidx.compose.runtime.LaunchedEffect` (`:65`) — still used at `:149`.
- **Remove** `android.widget.TextView` (`:11`). Line 721 is its only use and the helper is typed
  `findViewById<View>`. Keep it only if you deliberately make the helper `TextView`-specific.

### 4. `ScreenshotsTestSuite.kt`

`app/src/androidTest/java/de/lukasneugebauer/nextcloudcookbook/screenshots/ScreenshotsTestSuite.kt`

Add `getIngredientAt = { "" }` to the `RecipeDetailLayout` call at `:145` (named arguments there, so
placement is free). No screenshot changes.

**This file is not compiled by CI** — `fastlane build` is `clean → ktlintCheck → lint → test →
assemble`, and `assemble` does not build androidTest sources. Forgetting this leaves CI green and
breaks the `screenshots` lane later, so verify it locally (command below).

### 5. Tests — append to the file from task 1

Add spec §4 cases 1–4 to `RecipeDetailViewModelUnitTest.kt`, reusing the existing fixture:

**1. `getIngredientAt_afterIncreaseYield_returnsRecalculatedIngredient`** — the regression under test
- Act: `viewModel.increaseYield()` (4 → 5)
- Assert: `getIngredientAt(0) == "500 g flour"`, `getIngredientAt(1) == "2.5 eggs"`

**2. `getIngredientAt_withUnparsableIngredient_returnsIngredientUnchanged`**
- Act: `viewModel.increaseYield()`
- Assert: `getIngredientAt(2) == "salt"`

**3. `getIngredientAt_afterResetYield_returnsOriginalIngredient`**
- Act: `viewModel.increaseYield()` then `viewModel.resetYield()`
- Assert: `getIngredientAt(0) == "400 g flour"`

**4. `getIngredientAt_withOutOfRangeIndex_returnsEmptyString`**
- Act: none
- Assert: `getIngredientAt(99) == ""`

Be aware of what these do and do not cover: they test the ViewModel lookup, **not** the Compose
binding that actually failed. `connectedAndroidTest` does not run in CI, so the manual steps below
are the only end-to-end check. This is a known, accepted limitation of the spec — do not add an
instrumented test to compensate unless you also arrange for it to run.

## Verification

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
./gradlew test
./gradlew compileFullDebugAndroidTestKotlin   # CI does NOT do this — catches step 4
./gradlew assembleFullRelease                 # compile-checks the @Preview updates, as CI does
```

All six tests in `RecipeDetailViewModelUnitTest` must pass (cases 1–4 new, 5–6 from task 1 still
green).

### Manual verification (required — instrumented tests do not run in CI)

1. Open a recipe with a parsable ingredient, e.g. `400 g flour`, yield 4.
2. Tap **+** twice → the row reads `600 g flour`.
3. Long-press the row → clipboard contains `600 g flour`.
4. Tap **reset** → long-press again → clipboard contains `400 g flour`.
5. Long-press a `## Section` heading and an amount-less ingredient (e.g. `salt`) → both copy verbatim.
6. Share the recipe after changing servings → the shared text still shows recalculated amounts and
   the updated servings count (confirms §2.4 was not disturbed).

Test on an API ≤ 32 device or emulator as well if you can, so the `SDK_INT <= S_V2` toast path is
exercised.

## Done when

- Long-pressing an ingredient copies the currently displayed text (manual steps 1–4 pass).
- Ingredients without a parsable amount and `##` section headings copy unchanged (step 5).
- The whole-recipe share path still substitutes recalculated ingredients (step 6, and cases 5–6 green).
- `./gradlew ktlintCheck test compileFullDebugAndroidTestKotlin assembleFullRelease` all succeed.
- No `LaunchedEffect(Unit)` remains around the long-click registration. (The other
  `LaunchedEffect(Unit)` in the app, `RecipeListScreen.kt:468`, is a genuine one-shot — leave it.)

## Commit

Conventional commit with a `fix:` subject describing the user-visible behaviour, e.g.
`fix: copy recalculated amount when long-pressing an ingredient`. Release notes are derived from
commit messages; there is no `CHANGELOG.md` to edit. Reference #208 in the pull request rather than
the commit body. Do **not** touch `fastlane/metadata/android/*/changelogs/<versionCode>.txt` — those
are terse store-listing bullets written at release-prep time.
