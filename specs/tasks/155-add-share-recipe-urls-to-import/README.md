# Tasks — Share recipe URLs into the import screen

Breakdown of [`specs/spec/155-add-share-recipe-urls-to-import.md`](../../spec/155-add-share-recipe-urls-to-import.md)
(issue [#155](https://github.com/lneugebauer/nextcloud-cookbook/issues/155)).

## Approach

Three vertical slices, each independently mergeable and each leaving `main` in a shippable state.

The feature is a chain: shared text → URL → navigation argument → automatic import. The slices cut
that chain from the inside out, so that the riskiest logic lands first with the cheapest tests, and
the layer that can only be verified by hand lands last.

1. **The parsing rule** — a pure `String` extension with a JVM unit test. No Android, no UI, no
   dependants. This is the spec's "complex business logic" exception: it is verified entirely by the
   21-row table in §4.1 rather than by integration. That table covers self-hosted host forms — IPv4
   with a port, bracketed IPv6 literals — alongside the ordinary domain cases.
2. **The transport and the trigger** — the `sharedText` navigation argument, the ViewModel that
   consumes it, and the automatic import it fires. Fully covered by ViewModel unit tests. User-visible
   behaviour is unchanged apart from the back-stack fix of §2.7, because the only call site passes
   `null`.
3. **The entry point** — the manifest share target and the intent plumbing that finally supplies a
   non-null `sharedText`. This is where the feature becomes reachable, and it is the layer the spec
   deliberately verifies by hand (§4.3) rather than adding Robolectric.

Task 2 cannot compile without task 1's extension function, and task 3 cannot compile without task 2's
navigation argument, so the order is strict.

## Tasks

| # | Task | Depends on |
| --- | --- | --- |
| 1 | [`String.extractHttpUrl()` URL extraction](task-1-extract-http-url.md) | — |
| 2 | [`sharedText` nav argument and auto-import](task-2-shared-text-nav-argument.md) | Task 1 |
| 3 | [Share target registration and gated navigation](task-3-share-target-intent-handling.md) | Task 2 |

## Spec corrections already applied

The spec was amended before this breakdown was written; agents should read it as it now stands. Three
claims in the original draft were wrong or under-specified and are called out here so they are not
re-introduced from memory of the earlier version:

- **§2.5** originally claimed the `savedInstanceState == null` guard also stops `nccookbook://` deep
  links being re-handled after a rotation. It does not — the surviving `MainViewModel` still holds the
  Intent in `_intentState`, so the recreated composition's `LaunchedEffect(intent)` re-fires on the
  retained value regardless. The guard's real purpose is process-death restore.
- **§2.6** originally gated on "a destination other than `SplashScreenDestination`".
  `currentDestinationAsState()` emits `null` first, so that gate passes during the first frame of a
  cold start. It now requires non-null *and* not-splash.
- **§3.3** originally said "read `Intent.EXTRA_TEXT`". It now specifies `getCharSequenceExtra(...)
  ?.toString()`, because `getStringExtra()` returns `null` for the `Spanned` values that styled-text
  senders put in that extra.

## Scope notes

No API, DTO, repository, database, string-resource or `app/build.gradle` changes anywhere in this
breakdown (§3.8). `kotlinx-coroutines-test:1.10.1` is already a dependency, added by PR #207.
