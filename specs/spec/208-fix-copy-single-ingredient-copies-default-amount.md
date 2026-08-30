# Fix: copying a single ingredient copies the default amount

Issue: [#208 — Copying single ingredient always uses default amount](https://github.com/lneugebauer/nextcloud-cookbook/issues/208)

## 1. Goals & Requirements

### Problem

Long-pressing an ingredient on the recipe detail screen copies the amount for the recipe's
*original* yield, even after the user changed the servings. The displayed text is correct; only the
clipboard content is stale. Reported against app 0.30.0.

### Root cause (verified)

`RecipeDetailScreen.kt:720` registers the `TextView` long-click listener inside `LaunchedEffect(Unit)`.
Because the key is `Unit`, the effect never restarts, and the listener lambda holds the `ingredient`
`String` captured during the **first** composition of that slot.

The listener survives every recomposition: `MarkdownText` (twain 0.3.2) builds its `TextView` in the
`AndroidView` **`factory`** block and its `update` block only calls `markdownRender.setMarkdown(...)`.
So changing the servings re-renders the text but leaves the original listener — and its captured
string — in place.

`MarkdownText` exposes no `onClick`/`onLongClick` parameter; `viewId` + `findViewById` is the only
long-press hook the library offers. That constraint stays.

### Requirements

1. Long-pressing an ingredient copies the currently displayed (recalculated) text.
2. Ingredients without a parsable amount (`salt`) and section headings (`## Dough`) copy unchanged.
3. Resetting the servings returns the copied text to the original amount.
4. The whole-recipe share path is confirmed correct and gets a regression guard (see §2.4).

### Out of scope

Moving off twain, or replacing the `findViewById` long-press mechanism with an owned `AndroidView`.
The library gives no better hook today — see §2.1b for why, and for what a real replacement would
cost. The effect itself *is* corrected here (`LaunchedEffect` → `DisposableEffect`).

## 2. Architecture & Design Decisions

### 2.1a Resolve the copy text through the ViewModel, by index

**Decision.** `RecipeDetailViewModel` gains `getIngredientAt(index: Int): String`, resolved against
live state. `RecipeDetailLayout` and `Ingredients` gain a `getIngredientAt: (index: Int) -> String`
parameter; the ingredient loop becomes `forEachIndexed`; the long-click listener calls
`getIngredientAt(index)` instead of reading the captured string.

**Grounding:** convention. The ViewModel is already the single source of truth for
`calculatedIngredients` (`RecipeDetailState.kt:10`), and `getShareText()`
(`RecipeDetailViewModel.kt:130`) already resolves share content the same way — from
`_state.value` at call time rather than from a captured value.

**Why this fixes it.** The captured lambda holds a stable reference to the ViewModel plus an `Int`,
not a mutable `String`. The lookup runs at click time and sees current state.

**Index stability.** `YieldCalculatorImpl.recalculateIngredients` is a plain `ingredients.map { }`
(`YieldCalculatorImpl.kt:33`), so size and order are preserved across every recalculation.
Index-based lookup is safe.

**What this decision does and does not carry.** It is what makes the fix assertable in a JVM unit
test that runs in CI, and it keeps "what does this row represent" in the layer that owns that state.
It does **not** carry correctness on its own — §2.1b does that. Passing the index rather than the
resolved string is the safer shape and worth keeping, but the binding below is what makes a stale
capture impossible.

### 2.1b Remove the stale-capture hazard at its source

**Decision.** Extract the listener registration into a private helper in `RecipeDetailScreen.kt`,
and register with `DisposableEffect` rather than `LaunchedEffect`:

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

Write the listener body across two lines as shown, not as `{ currentOnLongClick(); true }` —
`ktlintCheck` runs before `test` in the `build` lane and the semicolon form is not worth the risk.

**Grounding:** convention and research. `KeepScreenOn` (`RecipeDetailScreen.kt:215`) is already this
exact shape — a private composable that emits no UI and exists only to wrap a `DisposableEffect`
keyed to a `Context` lookup — so the helper matches local style, and `app/.editorconfig` already
exempts `@Composable` functions from ktlint's function-naming rule. `rememberUpdatedState` is the
documented Compose remedy for a value captured by a long-lived callback that outlives the
composition that created it
([Compose side-effects guide](https://developer.android.com/develop/ui/compose/side-effects#rememberupdatedstate)).

**Effect timing is safe — verified, because this is the one way the change could silently break the
feature.** Swapping `LaunchedEffect` for `DisposableEffect` moves the `findViewById` from a
dispatched coroutine to a synchronous call during change application, so the interop `View` must
already be in the hierarchy. It is. `Composition.applyChangesInLocked` (runtime 1.10.0,
`Composition.kt:1116`–`1134`) applies all node changes and calls `applier.onEndChanges()` *before*
`rememberManager.dispatchRememberObservers()`, which is what runs a `DisposableEffect` block. Node
insertion attaches the `LayoutNode`, whose `onAttach` calls
`AndroidComposeView.addAndroidView` (`AndroidViewHolder.android.kt:445`), adding the holder to
`androidViewsHandler`; the holder already contains the `TextView`, added in its constructor
(`AndroidViewHolder.android.kt:115`). `Activity.findViewById` walks the decor tree, so the view
resolves.

This addresses three separate defects in the current code at `:720`:

1. **Staleness becomes impossible by construction.** `rememberUpdatedState` lives inside the helper,
   so the helper always invokes the newest lambda. A caller cannot capture a stale value, whatever
   it passes. This — not the index in §2.1a — is what fixes #208.
2. **The listener is now removed.** The current code never clears it. Minor in practice, since the
   `TextView` dies with the composition, but `DisposableEffect` is the correct primitive for
   register/unregister and costs one line.
3. **No coroutine for a synchronous registration.** `LaunchedEffect` launches a coroutine to call
   `setOnLongClickListener`. The coroutine buys nothing. (The clipboard write still needs
   `scope.launch` — `setClipEntry` is `suspend` — so `rememberCoroutineScope()` stays.)

Keyed on `viewId`, so `findViewById` runs once per view rather than per recomposition — which is why
`LaunchedEffect(ingredient)` was rejected as the fix.

**Why `findViewById` stays.** `MarkdownText` exposes no long-press parameter, and a Compose
`combinedClickable` wrapper is not a reliable substitute: Markwon's `CorePlugin.afterSetText`
installs a `LinkMovementMethod` when none is set (`CorePlugin.java:205`, markwon 4.6.2), so the
`TextView` consumes touch events. Ingredient values really can contain links —
`RecipeDetailViewModel.enrichRecipeLinks` rewrites `#r/123` into markdown links inside
`newIngredients`.

**Deliberately out of scope.** The structural fix is to own the `AndroidView`: a local markdown
composable whose `update` block re-binds the listener on every recomposition would delete
`generateViewId`, `findViewById`, and the effect outright. That means depending on Markwon directly
and reproducing twain's `createTextView` — colour resolution, font scaling, and the ellipsis
workaround at `MarkdownText.kt:120` — with visual-regression risk across all four `MarkdownText`
call sites. A refactor, not a bugfix; it belongs in its own change.

**Scope note.** This hazard occurs exactly once: `findViewById` appears only at `:721`, and the only
other `LaunchedEffect(Unit)` in the app (`RecipeListScreen.kt:468`, `focusRequester.requestFocus()`)
is a genuine one-shot over a stable capture. The helper is justified by correctness, not by reuse
count. Inlining `rememberUpdatedState` + `DisposableEffect` at the call site would get most of the
benefit if a single-use helper is unwelcome.

### 2.2 Clipboard write stays in the composable

**Decision.** The ViewModel returns a `String`; `LocalClipboard` and the pre-Android-13 `Toast`
stay in `Ingredients`.

**Grounding:** convention. `RecipeDetailScreen.kt:594` already uses `LocalClipboard`, and no
ViewModel in this project holds a `Context`. Moving the clipboard write would require injecting a
Context or adding a one-shot event channel for no gain.

### 2.3 Empty / out-of-range fallback

**Decision.** `getIngredientAt` reads `calculatedIngredients[index]`, falls back to
`data.ingredients[index].value`, and returns `""` when neither resolves. The long-click handler skips
the clipboard write and the toast on a blank result, still consuming the gesture.

**Grounding:** convention. This mirrors the existing `calculatedIngredients.ifEmpty { ... }` fallback
at `RecipeDetailScreen.kt:408`, which keeps `RecipeDetailLayout` renderable with
`calculatedIngredients = emptyList()` (used by `ScreenshotsTestSuite.kt:147` and the previews). In
production both fields are assigned in the same `_state` update, so the fallback is defensive only.

### 2.4 Whole-recipe copy: checked, already correct — add a guard

**Finding.** The share action is **not** affected. `getShareText()` (`RecipeDetailViewModel.kt:130`)
substitutes `state.calculatedIngredients` and `state.currentYield` into the recipe before handing it
to `RecipeFormatterImpl`, which renders the servings plural from `recipe.yield` and the list from
`ingredient.value`. It is re-evaluated on each recomposition at `RecipeDetailScreen.kt:190`.

The bulk "copy ingredients" button (`RecipeDetailScreen.kt:610`) is also correct: its `onClick`
lambda is a composable parameter, so it re-captures the current list on every recomposition. Only
`LaunchedEffect(Unit)` fails to.

**Decision.** No production change to either. Add unit coverage for `getShareText()` so the
substitution cannot silently regress.

### 2.5 New test dependency

**Decision.** Add `testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2'`.

**Grounding:** required. `RecipeDetailViewModel.init` calls `getRecipe(id)`, which ends in
`.launchIn(viewModelScope)` — `Dispatchers.Main.immediate`, unavailable on the JVM without
`Dispatchers.setMain`. Version matches the coroutines version already resolved for the project
(1.10.2). CI runs `fastlane build` → `gradle test`, so these tests run on every PR;
`connectedAndroidTest` does not run in CI, which is why the fix is covered by a ViewModel test plus
manual verification rather than an instrumented test.

## 3. Implementation Changes

### 3.1 `app/build.gradle`

Add to the `dependencies` block, next to the existing `testImplementation` entries (line ~177):

```groovy
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2'
```

### 3.2 `RecipeDetailViewModel.kt`

Add a public lookup beside `getShareText()`:

```kotlin
fun getIngredientAt(index: Int): String =
    _state.value.calculatedIngredients.getOrNull(index)?.ingredient
        ?: _state.value.data?.ingredients?.getOrNull(index)?.value
        ?: ""
```

Naming follows the existing `getShareText()` getter style on this class.

### 3.3 `RecipeDetailScreen.kt`

| Location | Change |
| --- | --- |
| `RecipeDetailLayout` signature (`:329`) | Add `getIngredientAt: (index: Int) -> String` |
| `RecipeDetailLayout` call in `RecipeDetailScreen` (`:162`) | Pass `getIngredientAt = viewModel::getIngredientAt` |
| `Ingredients` call (`:407`) | Forward `getIngredientAt` |
| `Ingredients` signature (`:585`) | Add `getIngredientAt: (index: Int) -> String` |
| Ingredient loop (`:678`) | `ingredients.forEach { (ingredient, hasCorrectSyntax) -> }` → `ingredients.forEachIndexed { index, (ingredient, hasCorrectSyntax) -> }` |
| Long-click listener (`:720`–`:748`) | Replace the whole `LaunchedEffect(Unit)` block with a `BindLongClick` call (§2.1b) |
| New private helper | Add `BindLongClick` as specified in §2.1b |
| `IngredientsPreview` (`:961`) | Pass `getIngredientAt = { "" }` |
| `RecipeDetailLayoutPreview` (`:1013`) | Pass `getIngredientAt = { "" }` |

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

`BindLongClick` holds the lambda in `rememberUpdatedState`, so this closure is re-read on every
long press. Resolving through `getIngredientAt(index)` rather than the captured `ingredient` is the
safer shape and stays, but the binding is what guarantees freshness — see §2.1b.

`context` and `LocalContext` move into the helper; `scope` (`rememberCoroutineScope()`) and
`clipboard` (`LocalClipboard`) stay in `Ingredients`, since `setClipEntry` is `suspend`.

Everything else in `Ingredients` — the checkbox, `MarkdownText`, the syntax indicator, the bulk copy
button — is unchanged. `ingredient` is still used for rendering.

Imports, checked against the current file:

- **Add** `androidx.compose.runtime.rememberUpdatedState` — the only genuinely new import.
- **Already present, leave alone:** `androidx.compose.runtime.DisposableEffect` (`:64`, used by
  `KeepScreenOn` at `:219`), `androidx.compose.runtime.getValue` (`:68`, needed for the `by`
  delegate), and `android.view.View` (`:9`, used by `View.generateViewId()` at `:688`).
- **Keep** `androidx.compose.runtime.LaunchedEffect` (`:65`) — still used at `:149`.
- **Remove** `android.widget.TextView` (`:11`). Line 721 is its only use, and the helper is typed
  `findViewById<View>`. Leave the import in place only if you deliberately keep the helper
  `TextView`-specific.

### 3.4 `ScreenshotsTestSuite.kt`

Add `getIngredientAt = { "" }` to the `RecipeDetailLayout` call at `:145`. No screenshot changes.

### 3.5 Housekeeping

- Run `./gradlew ktlintFormat` — `ktlintCheck` runs before `test` in the `build` lane.
- Commit with a conventional-commit `fix:` subject describing the user-visible behaviour. Release
  notes are derived from commit messages; there is no `CHANGELOG.md` to edit. Reference #208 in the
  pull request rather than the commit body — recent history does not put issue refs in commit bodies.
- Do **not** touch `fastlane/metadata/android/*/changelogs/<versionCode>.txt`. Those are terse
  store-listing bullets written at release-prep time (see `chore: prepare release v0.30.0+64`) and
  consumed by the `github` lane at `Fastfile:84` — out of scope for this change.

## 4. Test Cases

New file: `app/src/test/java/de/lukasneugebauer/nextcloudcookbook/recipe/presentation/detail/RecipeDetailViewModelUnitTest.kt`

There is no existing ViewModel test to extend. It follows `SyncRecipesUseCaseUnitTest` (plain
`mock()` fields assigned in `@Before`) rather than the `@Mock` + `MockitoAnnotations.openMocks` style,
since only a handful of collaborators are involved.

### Fixture

| Collaborator | Setup |
| --- | --- |
| `preferencesManager` | `mock()`, then `whenever(preferencesManager.preferencesFlow).thenReturn(flowOf(Preferences(...)))`. Copy the `Preferences` fixture verbatim from `RecipeRepositoryImplUnitTest.kt:770`–`787` — `ncAccount`, `recipeOfTheDay`, and `allowSelfSignedCertificates` are all required with no defaults. Stubbing this final `val` on a final class already works in this project (same file, line 778), so Mockito 5's inline mock maker is confirmed, not assumed. |
| `recipeFormatter` | `mock()` — the interface (`fun format(recipe: Recipe): String`), so no Android `Resources` needed |
| `recipeRepository` | `mock()`; `getRecipeFlow("1")` → `flowOf(DataResult.Success(RECIPE))`; `getRecipePreviewsFlow()` → `flowOf(DataResult.Success(emptyList()))` |
| `savedStateHandle` | `SavedStateHandle(mapOf("recipeId" to "1"))` — real instance, not a mock. Verified safe on the JVM: this constructor is annotated `@VisibleForTesting` ("should only be used directly in tests"), stores the map via `SavedStateHandleImpl`, and `get(key)` reads that plain map. Nothing touches `Bundle`, which matters because `testOptions.unitTests.returnDefaultValues` is **not** enabled in `app/build.gradle`. |
| `yieldCalculator` | Real `YieldCalculatorImpl(Locale("en"))` — matches `YieldCalculatorRecalculateIngredientsUnitTest:44`, and pins number formatting so expected strings are deterministic |

```kotlin
val RECIPE = emptyRecipe().copy(
    id = "1",
    name = "Pizza",
    yield = 4,
    ingredients = listOf(
        Ingredient(id = 0, value = "400 g flour", hasCorrectSyntax = true),
        Ingredient(id = 1, value = "2 eggs", hasCorrectSyntax = true),
        Ingredient(id = 2, value = "salt", hasCorrectSyntax = false),
    ),
)
```

`@Before` sets `Dispatchers.setMain(UnconfinedTestDispatcher())` and then constructs the ViewModel;
`@After` calls `Dispatchers.resetMain()`.

**Use `UnconfinedTestDispatcher`, not `StandardTestDispatcher`.** `init` → `getRecipe(id)` ends in
`.launchIn(viewModelScope)` on `Dispatchers.Main.immediate`. Every source in the fixture is a cold
`flowOf(...)` with no suspension points, so the unconfined dispatcher drains the whole flow during
construction and `_state` is fully populated by the time `@Before` returns. Tests are then plain
`@Test` functions with synchronous assertions — no `runTest`, no `advanceUntilIdle`, and no reliance
on `runTest` sharing its scheduler with the injected main dispatcher.

This matters for correctness, not just style: `increaseYield()` computes `currentYield + 1` from
current state, so acting before `init` settles would yield 1 instead of 5 and every expected string
would be wrong.

Expected recalculations at yield 4 → 5: `400 g flour` → `500 g flour`, `2 eggs` → `2.5 eggs`,
`salt` → `salt` (no parsable amount, returned unchanged).

> **Fixture caveat.** `YieldCalculatorImpl` formats through `NumberFormat.getNumberInstance`, which
> applies grouping separators. Keep recalculated amounts under 1000, or `1200 g` becomes `1,200 g`
> and the expected string changes.

### Cases

**1. `getIngredientAt_afterIncreaseYield_returnsRecalculatedIngredient`** — the regression under test.
- Act: `viewModel.increaseYield()` (4 → 5)
- Assert: `getIngredientAt(0) == "500 g flour"`; `getIngredientAt(1) == "2.5 eggs"`

**2. `getIngredientAt_withUnparsableIngredient_returnsIngredientUnchanged`**
- Act: `viewModel.increaseYield()`
- Assert: `getIngredientAt(2) == "salt"`

**3. `getIngredientAt_afterResetYield_returnsOriginalIngredient`**
- Act: `viewModel.increaseYield()` then `viewModel.resetYield()`
- Assert: `getIngredientAt(0) == "400 g flour"`

**4. `getIngredientAt_withOutOfRangeIndex_returnsEmptyString`**
- Act: none
- Assert: `getIngredientAt(99) == ""`

**5. `getShareText_afterIncreaseYield_usesRecalculatedIngredientsAndCurrentYield`** — guard for §2.4.
- Act: `viewModel.increaseYield()`, then `viewModel.getShareText()`
- Assert: `argumentCaptor<Recipe>()` on `verify(recipeFormatter).format(capture())`; captured
  `recipe.yield == 5` and `recipe.ingredients.map { it.value } == listOf("500 g flour", "2.5 eggs", "salt")`

**6. `getShareText_beforeYieldChange_usesOriginalIngredients`** — baseline.
- Assert: captured `recipe.yield == 4` and values equal the original three strings

### Manual verification (not automated — instrumented tests do not run in CI)

1. Open a recipe with a parsable ingredient, e.g. `400 g flour`, yield 4.
2. Tap **+** twice → the row reads `600 g flour`.
3. Long-press the row → clipboard contains `600 g flour`.
4. Tap **reset** → long-press again → clipboard contains `400 g flour`.
5. Long-press a `## Section` heading and an amount-less ingredient → both copy verbatim.
6. Share the recipe after changing servings → the shared text still shows recalculated amounts and
   the updated servings count (confirms §2.4 was not disturbed).

## 5. Readiness

**Ready to implement.** No `needs-research` markers remain. §2.1a and §2.5 were decided by the
maintainer; every other decision cites an existing file or pattern.

Claims verified against the codebase and dependency sources rather than assumed:

- twain 0.3.2 builds its `TextView` in the `AndroidView` **factory** and exposes no long-press
  parameter — read from the published sources jar. This is what makes the listener outlive
  recomposition, and why the `findViewById` hook must stay.
- Markwon 4.6.2's `CorePlugin.afterSetText` installs a `LinkMovementMethod` when none is set — read
  from the published sources jar (`CorePlugin.java:205`). This rules out a Compose
  `combinedClickable` wrapper as a substitute (§2.1b).
- `DisposableEffect` runs late enough for `findViewById` to resolve the interop view — traced through
  `Composition.applyChangesInLocked` and `AndroidViewHolder` in the runtime/ui 1.10.0 sources
  (§2.1b). This was the one way §2.1b could have silently broken the feature.
- The stale-capture hazard occurs exactly once: `findViewById` appears only at
  `RecipeDetailScreen.kt:721`, and the app's only other `LaunchedEffect(Unit)`
  (`RecipeListScreen.kt:468`) is a genuine one-shot.
- Import deltas for `BindLongClick` were checked line by line against the current file — only
  `rememberUpdatedState` is new; `DisposableEffect`, `getValue`, and `View` are already imported.
- `YieldCalculatorImpl.recalculateIngredients` is a plain `map`, so indices are stable (§2.1a).
- `SavedStateHandle(Map)` and the `PreferencesManager.preferencesFlow` stub both work in a plain JVM
  unit test (see the fixture table for the evidence).
- The expected recalculation strings were hand-traced through `YieldCalculatorImpl`'s decimal branch.

Two things a reviewer should weigh, both already accepted rather than open:

1. The fix is two layers (§2.1a + §2.1b) where one would do. `BindLongClick` alone closes #208;
   the ViewModel lookup alone would also close it, but only by convention. They are both kept
   deliberately: the helper makes staleness impossible, the lookup makes the behaviour assertable in
   CI. A reviewer who wants a smaller diff should drop §2.1a, not §2.1b — and accept that the
   regression then has no test at all.
2. The reported bug still has no automated regression test — the ViewModel tests cover the lookup,
   not the Compose binding that actually failed. `connectedAndroidTest` does not run in CI (§2.5), so
   the manual steps in §4 are the only end-to-end check.

The `LaunchedEffect(Unit)` hazard itself is now removed rather than worked around, and it occurred
exactly once in the app (§2.1b, scope note).

Not verified by execution: the tests have not been compiled or run, since the code they target does
not exist yet. Expect the usual first-run friction on the new `kotlinx-coroutines-test` dependency.
