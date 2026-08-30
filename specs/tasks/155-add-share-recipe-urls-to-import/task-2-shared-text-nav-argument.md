# Task 2 — `sharedText` nav argument and automatic import

**Spec:** [`specs/spec/155-add-share-recipe-urls-to-import.md`](../../spec/155-add-share-recipe-urls-to-import.md) — §2.4, §2.5, §2.7, §2.10, §3.5, §3.6, §3.7, §4.2

**Dependencies:** [Task 1](task-1-extract-http-url.md) must be merged first — this task calls
`String.extractHttpUrl()` from `core/util/StringExtractHttpUrlExtension.kt`.

## Goal

Give `DownloadRecipeScreen` a `sharedText` navigation argument, have `DownloadRecipeViewModel` consume
it to seed the form and fire the import automatically, and fix the back-stack handling that the share
entry point will depend on.

After this task the machinery is complete but not yet reachable: the only call site passes `null`, so
the manual import flow behaves exactly as before except for the back-stack fix. Task 3 supplies a
non-null value from a share intent.

## What to implement

### 1. `DownloadRecipeViewModel.kt` (§3.6)

Current constructor:

```kotlin
@HiltViewModel
class DownloadRecipeViewModel
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
    ) : ViewModel() {
```

Add `savedStateHandle: SavedStateHandle` **after** `recipeRepository`. The project orders constructor
parameters alphabetically — confirm against `RecipeDetailViewModel` and `RecipeListViewModel`, which
both place `savedStateHandle` this way and both take it without a `private val` because it is only
read in `init`.

Add an `init` block that:

1. reads `savedStateHandle.get<String>("sharedText")` and does nothing when it is `null` or blank;
2. computes `val url = sharedText.extractHttpUrl()`;
3. seeds `_uiState` with `DownloadRecipeScreenState.Initial(url = url ?: sharedText)`;
4. when `url != null` **and** `savedStateHandle.get<Boolean>("autoImportTriggered")` is not `true`,
   writes that flag back into the `SavedStateHandle` and calls `importRecipe()`.

**Ordering is load-bearing.** Seed `_uiState` *before* calling `importRecipe()`. `importRecipe()`
opens with `val currentState = _uiState.value` and does nothing unless it is
`DownloadRecipeScreenState.Initial`, taking the URL from that state — it takes no parameter. Calling
it first is a silent no-op, not a crash.

Read the existing `importRecipe()` before writing the `init` block so the interaction is clear. Do
**not** modify `importRecipe()`, `handleConflict()`, `dismissConflict()` or the `_conflict` flow —
they stay exactly as PR #207 left them. Because the automatic import goes through the ordinary
`importRecipe()` path, an HTTP 409 on a re-shared URL produces the conflict snackbar with no special
case (§2.10); that is the single most likely repeat action for a share target, so resist adding one.

The `autoImportTriggered` flag lives in `SavedStateHandle`, not in a field, because the hazard it
guards is process death (§2.5): a restored screen must not import the same URL a second time. A field
would be lost in exactly the case that matters.

### 2. `DownloadRecipeScreen.kt` (§3.5, §2.7)

Two changes to the `@Destination` composable only:

- Add `@Suppress("UNUSED_PARAMETER") sharedText: String?` between `navigator` and `viewModel`, with
  **no** Kotlin default value. The parameter is genuinely unused in the composable — the value reaches
  the ViewModel through `SavedStateHandle`, which is why the suppression is needed. This mirrors
  `RecipeDetailScreen`, which declares `@Suppress("UNUSED_PARAMETER") recipeId: String` for the same
  reason. Omitting the default is deliberate (§2.4): it mirrors the generated
  `RecipeListWithArgumentsScreenDestination(categoryName, keyword)` and avoids depending on
  default-value propagation through KSP.
- In the `onNavigateToDetail` lambda, change the nav options from `popUpTo(RecipeListScreenDestination)`
  to `popUpTo(DownloadRecipeScreenDestination) { inclusive = true }`, and drop the now-unused
  `RecipeListScreenDestination` import.

**The back-stack change is a required fix, not a cleanup.** Entered from a share on a cold start the
back stack is `Home → DownloadRecipe`; `RecipeListScreen` is not on it, and `popUpTo` on an absent
route is a no-op. The import screen would survive, and returning to it would re-run the
`LaunchedEffect(id)` in `DownloadRecipeScreenContent`'s `Loaded` branch and bounce the user straight
back to the recipe detail. Popping the import screen itself produces the identical result for the
existing entry point from the recipe list, so nothing regresses there.

`DownloadRecipeScreenContent`, `DownloadRecipeForm`, the conflict wiring and the `@Preview` are
untouched. `DownloadRecipeScreenContent` in particular must stay free of `sharedText` — PR #207 made
it stateless and it stays that way.

### 3. `RecipeListScreen.kt` (§3.7)

Around line 147:

```kotlin
onImportClick = { navigator.navigate(DownloadRecipeScreenDestination) },
```

becomes

```kotlin
onImportClick = { navigator.navigate(DownloadRecipeScreenDestination(sharedText = null)) },
```

This is not optional tidying. Today the generated destination is a `DirectionDestinationSpec` and can
be passed directly to `navigate()`. Once it declares an argument it becomes a
`TypedDestinationSpec<...>`, and the bare object no longer satisfies `navigate()` — the old line stops
compiling. This is the only call site; a repo-wide search for `DownloadRecipeScreenDestination`
confirms it.

`popUpTo(DownloadRecipeScreenDestination)` **does** keep compiling, because the generated object still
extends `BaseRoute` and `popUpTo` accepts a `RouteOrDirection`.

No manual URL encoding anywhere (§2.4). Compose Destinations 2.3.0 encodes `String` nav args when
building the route and the value arrives decoded — `stringNavType.get(savedStateHandle, key)` is
literally `savedStateHandle[key]`, which is why reading the handle directly matches what
`argsFrom(savedStateHandle)` would return. If you find yourself reaching for `URLEncoder`,
`URLDecoder`, or string surgery on `"sharedText="`, stop: PR #167 did that and it is explicitly
rejected.

## Tests (§4.2)

**Extend `app/src/test/java/de/lukasneugebauer/nextcloudcookbook/recipe/presentation/download/DownloadRecipeViewModelUnitTest.kt`.
Do not add a second file.**

Two adjustments to what is already there:

- `setUp()` currently builds `DownloadRecipeViewModel(recipeRepository)`. Give it an empty
  `SavedStateHandle()`. That keeps all four existing cases on the manual-entry path — no `sharedText`,
  no auto-import — so their assertions stay untouched.
- The new cases cannot reuse that shared instance, because the automatic import runs in `init`. Add a
  `createViewModel(savedStateHandle: SavedStateHandle)` helper and build a fresh ViewModel per case.

**Stub the repository before constructing the ViewModel.** This is the one ordering trap in the file:
the `init` import runs during construction, so a `whenever(...)` written after `createViewModel(...)`
stubs a mock that has already been called. The `UnconfinedTestDispatcher` field drains `init`
eagerly during construction, which is what makes each case a plain synchronous assertion on
`viewModel.uiState.value` afterwards.

Match the existing file's style, not that of any other test in the repo: backtick test names,
`runTest { }` bodies, the `UnconfinedTestDispatcher` field, and the `conflictResource` helper already
at the bottom of the file. `SavedStateHandle(mapOf(...))` works on the JVM, including writes, so the
nav argument needs no mocking.

New cases:

| Setup | Action | Expectation |
| --- | --- | --- |
| `sharedText` = `"https://example.com/r"`, repository returns `Resource.Success(emptyRecipeDto().copy(id = "42"))` | construct the ViewModel | `uiState` is `Loaded(id = "42")`; `importRecipe` called once with `ImportUrlDto("https://example.com/r")` |
| `sharedText` = `"Title https://example.com/r"` | construct the ViewModel | repository called with `ImportUrlDto("https://example.com/r")` — the title is not sent |
| `sharedText` = `"just some text"` | construct the ViewModel | `uiState` is `Initial(url = "just some text")`; `verifyNoInteractions(recipeRepository)` |
| empty `SavedStateHandle()` | construct the ViewModel | `uiState` is `Initial(url = "")`; repository never called — the manual-entry path is unchanged |
| `sharedText` = `"https://example.com/r"`, `"autoImportTriggered"` already `true` | construct the ViewModel | `uiState` is `Initial(url = "https://example.com/r")`; repository never called — the process-death guard of §2.5 |
| `sharedText` = `"https://example.com/r"` | construct the ViewModel | `savedStateHandle.get<Boolean>("autoImportTriggered")` is `true` afterwards — the flag is persisted, not just held in memory |
| `sharedText` = `"https://example.com/r"`, repository returns `Resource.Error` | construct the ViewModel | `uiState` is `Error(url = "https://example.com/r", …)` — the URL stays editable for a retry |
| `sharedText` = `"https://example.com/r"`, repository returns the 409 conflict resource (reuse the file's `conflictResource` helper) | construct the ViewModel | `conflict` is `ConflictState.Active`; `uiState` is `Initial(url = "https://example.com/r")` — the re-share path of §2.10 |

## Acceptance criteria

- [ ] `DownloadRecipeViewModel` takes `SavedStateHandle` and auto-imports exactly once for a shared URL.
- [ ] A non-URL payload seeds the form with the raw text and starts no download.
- [ ] The `autoImportTriggered` flag is written to the `SavedStateHandle`, so a restored ViewModel does not re-import.
- [ ] `DownloadRecipeScreen` declares `sharedText: String?` with no default; `DownloadRecipeScreenContent` does not mention it.
- [ ] `onNavigateToDetail` pops the import screen inclusively.
- [ ] `RecipeListScreen` passes `sharedText = null`.
- [ ] The four pre-existing ViewModel test cases still pass unmodified apart from `setUp()`.
- [ ] All eight new cases pass.
- [ ] No `app/build.gradle` change — `kotlinx-coroutines-test:1.10.1` is already there.

## Verification

```
./gradlew ktlintCheck test compileFullDebugAndroidTestKotlin
```

The `androidTest` compile step is not optional (§4.4): CI never compiles that source set, so a break
there is easy to miss. `ScreenshotsTestSuite` only instantiates layout composables and never
`DownloadRecipeScreen`, so the new argument should not reach it — this check is what confirms that. If
it does turn out to reach it, the fix is in the test suite, not by giving `sharedText` a default value.

Manual check of the unchanged entry point, since the back-stack behaviour changed for it too: from the
recipe list, tap import, paste a URL, download it. You should land on the recipe detail, and Back
should return to the recipe list — not to the import screen.
