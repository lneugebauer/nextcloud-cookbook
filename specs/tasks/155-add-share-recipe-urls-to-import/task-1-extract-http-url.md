# Task 1 — `String.extractHttpUrl()` URL extraction

**Spec:** [`specs/spec/155-add-share-recipe-urls-to-import.md`](../../spec/155-add-share-recipe-urls-to-import.md) — §2.3, §3.2, §4.1

**Dependencies:** none. This is the first task of the breakdown.

## Goal

Add the pure Kotlin extension that turns an arbitrary shared-text payload into the URL to import.
Shared text is rarely a bare URL — it is `"Best Lasagna https://example.com/lasagna"`, or a URL
followed by a newline and a page title. This extension owns the rule for picking the URL out of that,
and it is the only place in the codebase that will know that rule.

Nothing consumes it yet; task 2 wires it into `DownloadRecipeViewModel`. It is verified entirely by
its own unit test.

## What to implement

Create `app/src/main/java/de/lukasneugebauer/nextcloudcookbook/core/util/StringExtractHttpUrlExtension.kt`
with a single top-level function in package `de.lukasneugebauer.nextcloudcookbook.core.util`:

```kotlin
fun String.extractHttpUrl(): String?
```

Behaviour (§3.2):

- Match the **first** `http://` or `https://` token, **case-insensitively**, terminated by whitespace.
- Strip trailing `.` `,` `;` `:` `!` `?` `>` `"` `'` unconditionally, and a trailing `)` `]` `}` **only
  while the match holds more of that closing bracket than of its opener**.
- Return `null` when nothing matches, or when the remainder after the scheme is empty.
- Return the matched substring **verbatim** — do not lower-case it, do not normalise it. The
  `"HTTPS://EXAMPLE.COM/R"` case in the table asserts this.
- Validate nothing about the host. It may be a domain, a `.local` name, a bare IPv4 address, or a
  bracketed IPv6 literal, each with an optional `:port`. No IP parsing, no port range check, no host
  regex — the server decides what it can fetch.

The bracket balancing is what makes two rows of the table coexist. `(https://example.com/recipe)`
matches from after the `(`, so the token carries one unmatched `)` that must be stripped — while
`http://[fd00::1]` is an IPv6 literal carrying its own `[`, so its `]` must be kept. A blind strip of
every trailing bracket produces `http://[fd00::1`, which is a broken URL, and that is the shape a
self-hosted user on an IPv6 LAN would share. Counting the brackets present in the match handles both,
and incidentally keeps `https://en.wikipedia.org/wiki/Lasagne_(dish)` whole.

The `:` in `:8080` needs no special handling and must not get any: it is not trailing, and only
trailing punctuation is stripped. Do not add a port-aware branch.

Implementation constraints:

- Use a plain Kotlin `Regex` with `RegexOption.IGNORE_CASE`. Declare it as a private top-level `val`
  so it is compiled once rather than per call.
- **Do not use `android.util.Patterns.WEB_URL`.** PR #167 did, and it is rejected for two reasons
  (§2.3): it is an Android platform constant that is unavailable in plain JVM unit tests, and it
  matches scheme-less strings such as `example.com` that the Cookbook API cannot fetch. The whole
  point of this file being a plain Kotlin extension is that it is testable without Robolectric.
- Accept **both** `http` and `https`. PR #167 restricted to `https`, which would break self-hosted or
  LAN recipe sites.
- Whitespace-terminated means `\S`-style matching, which also terminates the token at a newline —
  the `"https://example.com/a\nCheck this out"` case depends on that.

Follow the shape of the existing sibling `core/util/StringAddSuffixExtension.kt`: no class, no object,
just the extension function, no KDoc unless something is genuinely non-obvious.

## Tests

Create `app/src/test/java/de/lukasneugebauer/nextcloudcookbook/StringExtractHttpUrlUnitTest.kt`.

**Note the location.** The existing string-extension tests do *not* mirror the main-source package
path — `StringAddSuffixUnitTest.kt` and `StringParseAsDurationUnitTest.kt` both sit directly in the
root test package `de.lukasneugebauer.nextcloudcookbook` and import the function under test from
`core.util`. Follow that, not the `core/util/` path.

Follow those two files' conventions: JUnit 4, no mocks, plain `assertEquals`, and their
`string_DoesSomething_ReturnsSomething()` underscore naming. This is deliberately **not** the backtick
naming used by `DownloadRecipeViewModelUnitTest` — match the sibling string-extension tests, per §4.1.

Cover every row of the §4.1 table:

| Input | Expected |
| --- | --- |
| `"https://example.com/recipe"` | `"https://example.com/recipe"` |
| `"http://cookbook.local/recipe"` | `"http://cookbook.local/recipe"` |
| `"http://192.168.1.50/recipe"` | `"http://192.168.1.50/recipe"` (bare IPv4 host) |
| `"http://192.168.1.50:8080/recipe"` | `"http://192.168.1.50:8080/recipe"` (port preserved — the `:` is not trailing) |
| `"http://192.168.1.50:8080"` | `"http://192.168.1.50:8080"` (host and port, no path) |
| `"Recipe on the NAS http://192.168.1.50:8080/r/42."` | `"http://192.168.1.50:8080/r/42"` (trailing dot trimmed, port intact) |
| `"https://[fd00::1]:8080/recipe"` | `"https://[fd00::1]:8080/recipe"` (bracketed IPv6 literal) |
| `"http://[fd00::1]"` | `"http://[fd00::1]"` (the `]` closes the literal, it is not trailing punctuation) |
| `"Best Lasagna https://example.com/lasagna"` | `"https://example.com/lasagna"` |
| `"https://example.com/a\nCheck this out"` | `"https://example.com/a"` |
| `"https://a.example.com/x https://b.example.com/y"` | `"https://a.example.com/x"` |
| `"https://example.com/r?portion=4&unit=g"` | `"https://example.com/r?portion=4&unit=g"` (query preserved) |
| `"Look at https://example.com/recipe."` | `"https://example.com/recipe"` (trailing dot trimmed) |
| `"(https://example.com/recipe)"` | `"https://example.com/recipe"` |
| `"https://en.wikipedia.org/wiki/Lasagne_(dish)"` | `"https://en.wikipedia.org/wiki/Lasagne_(dish)"` (balanced parens kept) |
| `"HTTPS://EXAMPLE.COM/R"` | `"HTTPS://EXAMPLE.COM/R"` (case-insensitive match, value untouched) |
| `"example.com/recipe"` | `null` (no scheme) |
| `"192.168.1.50:8080/recipe"` | `null` (no scheme — an IP is not special-cased) |
| `"Some lovely recipe"` | `null` |
| `""` | `null` |
| `"https://"` | `null` |

Some of these rows exist specifically to catch a naive regex or a naive strip, so do not drop them
when condensing:

- the query-string row — `?` and `&` must survive, since `?` is in the trailing-punctuation set but
  only trailing occurrences may be stripped;
- `"(https://example.com/recipe)"` — the leading `(` must not become part of the match, and the
  trailing `)` must;
- `"http://[fd00::1]"` against `"https://[fd00::1]:8080/recipe"` — the pair that forces bracket
  counting rather than a blind trailing strip;
- `"http://192.168.1.50:8080"` — a match ending in a port, where the preceding `:` must not tempt any
  special handling;
- `"192.168.1.50:8080/recipe"` — a scheme-less IP returns `null` like any other scheme-less input.
  This is deliberate, not an oversight: the payload then reaches the import screen as prefilled text
  the user can correct (requirement 4).

This table is the complete contract. Behaviour outside it — bare `www.` hosts, URLs embedded
mid-token, punctuation *inside* a path, IP or port validity — is not specified and needs no handling.

## Acceptance criteria

- [ ] `String.extractHttpUrl()` exists in `core/util/StringExtractHttpUrlExtension.kt` and returns `String?`.
- [ ] All 21 table rows pass as unit tests.
- [ ] IPv4-with-port, IPv6-literal and no-path hosts round-trip unchanged; no host or port validation was added.
- [ ] No reference to `android.util.Patterns` anywhere in the change.
- [ ] The test runs on the plain JVM — no Robolectric, no new dependency in `app/build.gradle`.

## Verification

```
./gradlew ktlintCheck test
```

Both must pass. The new test class must appear in the run — confirm it by name rather than assuming,
e.g. by checking `app/build/reports/tests/` or running with `--tests '*StringExtractHttpUrl*'`.
