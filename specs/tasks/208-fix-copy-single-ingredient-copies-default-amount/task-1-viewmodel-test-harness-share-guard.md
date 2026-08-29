# Task 1 — ViewModel test harness + whole-recipe share regression guard

**Spec:** `specs/spec/208-fix-copy-single-ingredient-copies-default-amount.md` — read §2.4
(whole-recipe copy is already correct), §2.5 (new test dependency), §3.1, and §4 (fixture table +
cases 5 and 6).

**Dependencies:** none. This is the first task.

## Goal

Stand up the project's first `RecipeDetailViewModel` JVM unit test and lock in that `getShareText()`
substitutes the **recalculated** ingredients and the **current** yield into the recipe before
formatting. Spec §2.4 verified this path is not affected by issue #208; this task makes that
non-regression assertable in CI.

No production code changes in this task. `getIngredientAt` and the Compose fix belong to
[task 2](task-2-fix-stale-ingredient-copy.md).

## What to implement

### 1. `app/build.gradle`

Add next to the existing `testImplementation` entries (currently `junit:junit:4.13.2`,
`org.mockito.kotlin:mockito-kotlin:5.1.0`, `org.mockito:mockito-core:5.1.0` at ~line 177):

```groovy
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2'
```

This is required, not optional: `RecipeDetailViewModel.init` calls `getRecipe(id)`, which ends in
`.launchIn(viewModelScope)` — `Dispatchers.Main.immediate`, which does not exist on the JVM without
`Dispatchers.setMain`. 1.10.2 matches the coroutines version already resolved for the project.

### 2. New file

`app/src/test/java/de/lukasneugebauer/nextcloudcookbook/recipe/presentation/detail/RecipeDetailViewModelUnitTest.kt`

Follow the style of `app/src/test/java/de/lukasneugebauer/nextcloudcookbook/core/domain/usecase/SyncRecipesUseCaseUnitTest.kt`:
plain `private lateinit var` fields assigned with `mock()` in `@Before`, **not** the `@Mock` +
`MockitoAnnotations.openMocks` style. Only a handful of collaborators are involved.

#### Fixture

| Collaborator | Setup |
| --- | --- |
| `preferencesManager` | `mock()`, then `whenever(preferencesManager.preferencesFlow).thenReturn(flowOf(Preferences(...)))`. Copy the `Preferences` construction verbatim from `app/src/test/java/de/lukasneugebauer/nextcloudcookbook/recipe/data/repository/RecipeRepositoryImplUnitTest.kt:778`–`787` — `isShowIngredientSyntaxIndicator`, `ncAccount`, `recipeOfTheDay`, and `allowSelfSignedCertificates` are all required with no defaults. Stubbing this final `val` on a final class already works in this project (same file), so Mockito's inline mock maker is confirmed. |
| `recipeFormatter` | `mock()` — it is the `RecipeFormatter` interface (`fun format(recipe: Recipe): String`), so no Android `Resources` are needed. |
| `recipeRepository` | `mock()`; stub `getRecipeFlow("1")` → `flowOf(DataResult.Success(RECIPE))` and `getRecipePreviewsFlow()` → `flowOf(DataResult.Success(emptyList()))`. Both stubs must be in place **before** the ViewModel is constructed, since `init` starts collecting. |
| `savedStateHandle` | `SavedStateHandle(mapOf("recipeId" to "1"))` — a real instance, not a mock. This constructor is `@VisibleForTesting` and stores the map directly; nothing touches `Bundle`, which matters because `testOptions.unitTests.returnDefaultValues` is **not** enabled in `app/build.gradle`. |
| `yieldCalculator` | Real `YieldCalculatorImpl(Locale("en"))` — matches `YieldCalculatorRecalculateIngredientsUnitTest` and pins number formatting so expected strings are deterministic. |

Recipe fixture (`emptyRecipe()` lives in
`app/src/main/java/de/lukasneugebauer/nextcloudcookbook/recipe/util/EmptyRecipe.kt`):

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

Confirm the `Ingredient` constructor parameters against
`app/src/main/java/de/lukasneugebauer/nextcloudcookbook/recipe/domain/model/Ingredient.kt` before
writing them; if they differ, use the real ones.

#### Dispatcher

`@Before` calls `Dispatchers.setMain(UnconfinedTestDispatcher())` **and then** constructs the
ViewModel. `@After` calls `Dispatchers.resetMain()`.

Use `UnconfinedTestDispatcher`, not `StandardTestDispatcher`. Every source in the fixture is a cold
`flowOf(...)` with no suspension points, so the unconfined dispatcher drains the whole flow during
construction and `_state` is fully populated by the time `@Before` returns. Tests are then plain
`@Test` functions with synchronous assertions — no `runTest`, no `advanceUntilIdle`.

This is a correctness requirement, not a style preference: `increaseYield()` computes
`currentYield + 1` from current state, so acting before `init` settles would produce yield 1 instead
of 5 and every expected string would be wrong.

#### Test cases

**5. `getShareText_afterIncreaseYield_usesRecalculatedIngredientsAndCurrentYield`** (the §2.4 guard)
- Act: `viewModel.increaseYield()`, then `viewModel.getShareText()`
- Assert with `argumentCaptor<Recipe>()` on `verify(recipeFormatter).format(capture())`:
  captured `recipe.yield == 5` and
  `recipe.ingredients.map { it.value } == listOf("500 g flour", "2.5 eggs", "salt")`

**6. `getShareText_beforeYieldChange_usesOriginalIngredients`** (baseline)
- Act: `viewModel.getShareText()` with no yield change
- Assert: captured `recipe.yield == 4` and
  `recipe.ingredients.map { it.value } == listOf("400 g flour", "2 eggs", "salt")`

Keep the numbering/naming from the spec so task 2 can append cases 1–4 to the same file without
renaming anything.

> **Fixture caveat.** `YieldCalculatorImpl` formats through `NumberFormat.getNumberInstance`, which
> applies grouping separators. Keep recalculated amounts under 1000, or `1200 g` becomes `1,200 g`
> and the expected string changes.

## Verification

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
./gradlew testDebugUnitTest --tests '*RecipeDetailViewModelUnitTest*'
./gradlew test
```

Both new tests must pass with the assertions above actually evaluated — if `getShareText()` returns
early (`_state.value.data` null), `verify(recipeFormatter).format(...)` fails, which is the signal
that the fixture did not settle. Do not "fix" that by relaxing the assertion; fix the fixture.

The spec has not been executed against real code, so expect first-run friction on the new
`kotlinx-coroutines-test` dependency (Gradle sync, import resolution). That is normal — resolve it
rather than working around it.

## Done when

- `kotlinx-coroutines-test:1.10.2` is a `testImplementation` dependency.
- `RecipeDetailViewModelUnitTest.kt` exists with the fixture above and cases 5 and 6 passing.
- `./gradlew ktlintCheck test` is green.
- No file under `app/src/main/` was modified.

## Commit

Conventional commit. This task adds coverage only, e.g.
`test: cover recipe share text against yield changes`. Reference #208 in the pull request, not in
the commit body — recent history does not put issue refs in commit bodies. There is no
`CHANGELOG.md` and store-listing changelogs under `fastlane/metadata/` are out of scope.
