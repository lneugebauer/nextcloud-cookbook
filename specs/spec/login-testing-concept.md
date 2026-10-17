# Concept: testing the login flow

Status: concept, nothing implemented yet. Meant as the base for one or more follow-up specs.

## 1. What "login" is in this app

The login flow is three screens and two background observers:

| Step | Code | What it does |
|---|---|---|
| Start screen | `auth/presentation/start/StartScreenViewModel.kt` | Takes the server URL, validates it (`Patterns.WEB_URL`), persists the "allow self-signed certificates" switch, routes to one of the two sign-in methods |
| Browser login (Login Flow v2) | `auth/presentation/browser/BrowserLoginViewModel.kt`, `auth/data/repository/AuthRepositoryImpl.kt`, `auth/data/api/AuthApi.kt` | `POST {url}/index.php/login/v2`, opens the returned `login` URL in a Custom Tab (`core/util/UriOpenInCustomTabExtension.kt`), polls `poll.endpoint` with `token` every 5 s until the server returns `{server, loginName, appPassword}`, stores that as `NcAccount` |
| Manual login (token) | `auth/presentation/manual/ManualLoginViewModel.kt` | Takes username + app password, stores them as `NcAccount` right away |
| Authorization observer | both login ViewModels, `observeAuthorizationStatus()` | Once an account exists and `NcCookbookApiProvider` has built a Retrofit API for it, calls `GET ocs/v2.php/cloud/user`. Success → `Authenticated`, failure → clears preferences and shows the error |
| TLS trust | `core/util/OkHttpClientProvider.kt` | Rebuilds the shared `OkHttpClient` with a trust-all `X509TrustManager` + permissive hostname verifier whenever the self-signed preference flips |

The transport variants you listed only touch two places in that chain:

| Variant | Where it is actually decided |
|---|---|
| valid https | default `OkHttpClient` from `di/AppModule.kt`, system + user trust anchors (`res/xml/network_security_config.xml`) |
| self-signed https | `OkHttpClientProvider.configureTrustAllCertificates` after the switch is ticked; and, for browser login, the Custom Tab's own interstitial (not under app control, see spec 183 §2.3) |
| http | `usesCleartextTraffic` / `cleartextTrafficPermitted="true"` in the manifest and network security config; `StartScreenViewModel.isValidUrl` only requires an `http://` or `https://` prefix |
| domain + port, IP + port | nothing special in the app: the URL is a string that goes through `Patterns.WEB_URL`, then `AuthRepositoryImpl.getLoginEndpoint` builds `{url}/index.php/login/v2`, then `LoginResponse.toLoginResult` normalises the `server` value the Nextcloud reports back (trailing slash) |

So the 5 × 2 matrix (transport × method) is really:

- **URL handling**: 5 input shapes that must survive validation, endpoint building, and the round trip through the `server` field that Nextcloud returns.
- **TLS trust**: 3 states (trusted chain, untrusted chain + switch on, untrusted chain + switch off) × 2 (https, plain http, where the switch is irrelevant).
- **Method**: Login Flow v2 polling vs direct token storage, each followed by the same `getUserMetadata` check.

That decomposition is what keeps the test count sane: the full matrix is covered cheaply at the HTTP level on the JVM, and only a handful of cells are repeated end-to-end against a real Nextcloud.

## 2. Test layers

### Layer 1: JVM unit tests (exists, extend)

What is there: `BrowserLoginViewModelUnitTest` covers the Flow v2 state machine, poll retry, process-death resume, and the non-web login URL guard. Pattern: plain Mockito mocks, `StandardTestDispatcher`, `runTest`, no Robolectric.

What is missing:

- `ManualLoginViewModelUnitTest`: empty username/password validation, URL missing from `SavedStateHandle`, trailing slash removal, `Authenticating` → `Authenticated` once `accountRepository`/`apiProvider` emit, `getUserMetadata` failure clears preferences and keeps the username for retry.
- `StartScreenViewModelUnitTest`: URL validation table (see the blocker below), event routing, self-signed switch persisted on init and on change.
- `LoginResponseUnitTest`: `toLoginResult` with and without trailing slash, with port, with sub-path (`https://host/nextcloud`).
- `AuthRepositoryImplUnitTest` for endpoint building only (`index.php` already present, trailing slash, sub-path) with a mocked `AuthApi`.

Blocker: `StartScreenViewModel.isValidUrl` calls `android.util.Patterns.WEB_URL`, which is a stub in the unit test `android.jar` and throws. Two ways out, decide once:

1. Move the validation into a pure Kotlin function (for example `core/util/ServerUrlValidator.kt`) that uses `okhttp3.HttpUrl.parse` or a small regex, and unit test that. Preferred: it also removes a known gap, because `Patterns.WEB_URL` rejects single-label hosts such as `http://nas:8080` or `http://localhost:8080`, which are legitimate home-network addresses. Whether that gap is a bug to fix is a separate decision, but a test table makes it visible either way.
2. Add Robolectric for this one class. Heavier, slower, and nothing else in `app/src/test` needs it.

Cost: minutes per test, runs in `./gradlew test`, already in CI via `fastlane build`.

### Layer 2: JVM integration tests against a local HTTP server (new, the workhorse)

Run the real `AuthRepositoryImpl` + `AuthApiProvider` + `OkHttpClientProvider` + `AccountRepositoryImpl` against OkHttp's `MockWebServer`, with `okhttp-tls` to mint certificates. Everything stays on the JVM and in `./gradlew test`, so it runs in the existing CI job with no emulator.

What it proves per matrix cell:

| Cell | How the test sets it up | What is asserted |
|---|---|---|
| http, IP + port | `MockWebServer` on `127.0.0.1:<random>` | request path is `/index.php/login/v2`, poll request carries `token`, resulting `NcAccount.url` is what the server returned (with port) |
| http, domain + port | same server, URL built with a hostname (`localhost` or a `Dns` override in the client) | same as above, plus the `Host` header |
| valid https | `MockWebServer.useHttps()` with a `HeldCertificate` signed by a test CA; the client is a copy of the production `OkHttpClient` with that CA added via `HandshakeCertificates` | handshake succeeds with the switch **off** |
| self-signed https, switch on | same server, client built by the real `OkHttpClientProvider` after `allowSelfSignedCertificates = true` | handshake succeeds |
| self-signed https, switch off | same server, default client | `Resource.Error` with `R.string.error_ssl_handshake` (from `BaseRepository.handleResponseError`) |
| sub-path install (`https://host/nextcloud`) | server responds under `/nextcloud/index.php/login/v2` | endpoint building keeps the prefix, `server` round-trips |
| Flow v2 polling | enqueue `404`, `404`, `200 {server, loginName, appPassword}` | three poll requests, account stored |
| redirects | enqueue `301` to the `index.php` URL | `NetworkInterceptor` turns it into `308` and OkHttp re-POSTs (regression guard for reverse proxies that redirect `/login/v2` → `/index.php/login/v2`) |
| token login | `GET ocs/v2.php/cloud/user` with `Authorization: Basic` | the stored username/app password reach the server as basic auth, and a `401` ends in `clearPreferences` |

Infrastructure needed:

- `testImplementation 'com.squareup.okhttp3:mockwebserver'` and `'com.squareup.okhttp3:okhttp-tls'` (both in the OkHttp BOM already declared).
- A small test fixture that builds the production `OkHttpClient` the same way `AppModule.provideOkHttpClient` does. Easiest: make that builder a top-level function the module calls, so the test and the app share it.
- `OkHttpClientProvider` and `AuthApiProvider` take `PreferencesManager` (needs a `Context`) and a `CoroutineScope`. For these tests either mock `PreferencesManager.preferencesFlow` with a `MutableStateFlow` (Mockito 5 mocks final classes out of the box) or introduce a tiny `interface SelfSignedPreference { val flow: Flow<Boolean> }`. Mocking is fine to start.
- The `Uri` problem from Layer 1 reappears in `LoginEndpointResponse.toLoginEndpointResult` (`login.toUri()`). Same `mockStatic(Uri::class.java)` trick as in the existing test, or keep `loginUrl` as a `String` in the domain model and parse it in the ViewModel. The second is cleaner and removes a static mock from every test that touches the repository.

Cost: each test runs in tens of milliseconds, no device, no Docker. This layer gives the full 5 × 2 matrix plus the error cases for essentially free, and it is the layer to invest in first.

### Layer 3: instrumented end-to-end against a real Nextcloud (new, small and slow)

Only this layer proves that the app, a real Nextcloud, the Cookbook server app, and the system's network stack agree. Keep it to a handful of scenarios; the matrix is already covered by Layer 2.

**Server side: Docker Compose, checked into the repo (`e2e/docker-compose.yml`).**

- `nextcloud:<pinned>-apache` with `SQLITE_DATABASE`, `NEXTCLOUD_ADMIN_USER`, `NEXTCLOUD_ADMIN_PASSWORD`, `NEXTCLOUD_TRUSTED_DOMAINS` covering every host name the tests use. A one-shot init container runs `occ app:install cookbook`, creates a test user, and creates an app password for it (`occ user:add-app-password`, verify it exists on the pinned version; if not, the user's plain password works for basic auth as long as 2FA is off).
- `caddy` in front of it with `tls internal` → a certificate from Caddy's own local CA, which no device trusts. That is the self-signed https case. Caddy serves it on one port and proxies plain http on another, so the same Nextcloud backs both the `http` and `self-signed https` cells. Nextcloud needs `OVERWRITEPROTOCOL`/`OVERWRITEHOST` per entry point for the `login` and `server` URLs it generates; the simplest is two Caddy entry points with `trusted_proxies` and `overwrite.cli.url` left unset.
- Same file serves local development: `docker compose -f e2e/docker-compose.yml up -d` and the stack is ready in about a minute.

**Reaching the server from the emulator.**

- IP + port: the emulator reaches the host at `10.0.2.2`, so `http://10.0.2.2:8080` and `https://10.0.2.2:8443` need no setup. On a physical device use the machine's LAN IP, passed in as an instrumentation argument.
- Domain + port: the emulator has no name for the host. Options, pick one:
  1. Start the emulator with `-dns-server 10.0.2.2` and run a `dnsmasq` container that answers `nextcloud.test` → `10.0.2.2`. Works on every emulator image, no root, no external dependency. Recommended.
  2. Use a wildcard DNS service such as `nextcloud.10.0.2.2.nip.io`. Zero setup but depends on public DNS and on nip.io being reachable from CI.
  3. `adb root` + `-writable-system` and push an `/etc/hosts`. Only works on AOSP (`default`) images, not on Google APIs images.
- Valid https: a publicly trusted certificate for `10.0.2.2` is impossible. Options:
  1. Do not run this cell end-to-end; Layer 2 already proves the trusted-chain path and nothing in the app treats a "valid" certificate differently from "a CA the device trusts". Recommended to start.
  2. Install Caddy's root CA as a user certificate on the emulator. The app's network security config already trusts user certificates, so the app then sees it as valid. Installation needs a UiAutomator walk through Settings (or root on an AOSP image), which is the fragile part.
  3. A nightly job against a real, publicly reachable test instance with credentials in repository secrets. Only worth it if such an instance exists.

**The Custom Tab problem.** Login Flow v2 opens a browser, and a browser is the one thing an instrumentation test cannot drive from Compose. Two approaches:

- **A. Drive the browser with UiAutomator.** Needs a Google APIs image (Chrome), dismissing Chrome's first-run screens, clicking through the certificate interstitial for the self-signed case, typing credentials into Nextcloud's HTML, tapping "Grant access". This is the only automation that exercises the real Custom Tab and the return-to-app intent. It is also the flakiest piece in the whole concept, and it still cannot cover passkeys (the Android credential sheet needs an enrolled authenticator).
- **B. Stub the browser and approve the flow over HTTP.** Register `Espresso-Intents` (`intending(hasAction(ACTION_VIEW)).respondWith(...)`) so `CustomTabsIntent.launchUrl` succeeds without a browser installed, capture the launched URL, and let the test complete the sign-in itself with an OkHttp client and a cookie jar: open the flow URL, submit the login form with the test user's credentials and the page's `requesttoken`, post the grant form. The app meanwhile polls and signs in exactly as in production. Approximately four requests against Nextcloud's HTML; confirm the exact sequence against the pinned version once, then it is stable. Works on the cheaper AOSP image, no Chrome, no interstitial. Does not test the Custom Tab itself, which Layer 4 keeps.

Recommendation: B for everything that runs in CI, and treat A as an optional local-only smoke test (a Gradle task or a test annotated `@LargeTest` filtered out in CI) if it turns out to be worth its upkeep. The parts that only A would catch, passkeys and return-to-app across Android versions, cannot be automated anyway and stay in the manual checklist from spec 183 §4.3.

**Scenarios for Layer 3** (one test each, roughly 20 to 40 s per test on an emulator):

1. Flow v2, http, IP + port: enter URL, sign in, approve, land on Home, recipe list from the Cookbook server app loads.
2. Flow v2, self-signed https, domain + port, switch on: same, through Caddy.
3. Flow v2, self-signed https, switch off: start screen → error with `error_ssl_handshake`, no poll ever started.
4. Token login, http: manual sign in with the occ-created app password, land on Home.
5. Token login, self-signed https, switch on, wrong password: error shown, preferences cleared, username kept.
6. Process death during Flow v2 (`adb shell am kill` from the test via `UiDevice.executeShellCommand` or `ActivityScenario.recreate()`), approve, app resumes polling the saved token and signs in. This is the one case spec 183 could only verify by hand.

**Test harness pieces.**

- `androidx.test.espresso:espresso-intents` for B.
- `createAndroidComposeRule<MainActivity>()` is already a dependency (`ui-test-junit4`). Add `Modifier.testTag(...)` to the URL field, the two sign-in buttons, the self-signed switch, username/password fields, and the error retry button; there are none today. Finding nodes by text works but breaks on every translation.
- Server URLs, user and app password come in as instrumentation arguments (`-Pandroid.testInstrumentationRunnerArguments.ncUrlHttp=...`) with defaults for the Compose stack, so the same test runs locally and in CI.
- A `@Before` that clears DataStore (`PreferencesManager`) and the Room database so every test starts logged out; a `@After` that revokes the app passwords the flow created (`occ user:delete-app-password` or just a fresh user per run) so the server does not accumulate tokens.
- Hilt needs no replacement here because the server URL is user input. If a later scenario needs a swapped module (the SSO case below does), add `hilt-android-testing`, a `HiltTestRunner`, and `@HiltAndroidTest` then, not now.
- Keep these tests out of `ScreenshotsTestSuite` and out of the default `connectedAndroidTest` run of whoever has an emulator open: put them in their own package (`androidTest/.../e2e`) and run them with `-Pandroid.testInstrumentationRunnerArguments.package=...e2e`.

### Layer 4: manual checklist (exists)

Spec 183 §4.3 already lists what only a human with real devices can verify: passkeys against a real IdP, security keys, the Custom Tab interstitial, return-to-app on Android 13 and 15+. Move that list into `docs/` or `specs/` as a standing release checklist and reference it from here. Nothing in Layers 1 to 3 replaces it.

## 3. Coverage map

| Cell | L1 unit | L2 JVM + MockWebServer | L3 emulator + Docker | L4 manual |
|---|---|---|---|---|
| valid https, Flow v2 | state machine | trusted CA handshake | not in CI (see §2, options) | real server |
| valid https, token | validation | basic auth round trip | not in CI | |
| self-signed https, Flow v2, switch on | | trust-all handshake | scenario 2 | interstitial |
| self-signed https, Flow v2, switch off | | `error_ssl_handshake` | scenario 3 | |
| self-signed https, token | | trust-all + basic auth | scenario 5 | |
| http, Flow v2 | | plain http | scenario 1 | |
| http, token | | plain http | scenario 4 | |
| domain + port | URL validation table | `Host` header, `server` round trip | scenario 2 | |
| IP + port | URL validation table | port preserved in `NcAccount.url` | scenarios 1, 4 | |
| sub-path install | URL validation table | prefix preserved | | |
| polling: 404 then 200 | retry timing | request count | scenarios 1, 2 | |
| process death | saved-state resume | | scenario 6 | |
| passkey / WebAuthn | | | | only here |
| return to app after Custom Tab | | | | only here |

## 4. Local developer workflow

Goal: one command per layer, nothing beyond Docker and an emulator or device.

```
./gradlew testFullDebugUnitTest                      # Layers 1 and 2, seconds
docker compose -f e2e/docker-compose.yml up -d --wait
./gradlew connectedFullDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.package=de.lukasneugebauer.nextcloudcookbook.e2e
docker compose -f e2e/docker-compose.yml down -v
```

Wrap the last three in a devbox script (`app:e2e`) next to `app:screenshots`, and give `process-compose.yaml` an `e2e:stack` process so `devbox services up` brings the Nextcloud up alongside the docs server. A physical device works the same way with the host's LAN IP passed as the URL argument; the self-signed case then also needs `NEXTCLOUD_TRUSTED_DOMAINS` to include that IP, which the compose file can take from an environment variable.

## 5. CI design

Keep the existing `fastlane` job untouched; Layers 1 and 2 land in it automatically through `fastlane build` → `test`.

Add a second job `e2e` to `.github/workflows/ci.yml`:

1. `actions/checkout`, JDK 17, Gradle cache.
2. Enable KVM on the runner (the `udev` rule step from the `reactivecircus/android-emulator-runner` README; `ubuntu-latest` supports it).
3. `docker compose -f e2e/docker-compose.yml up -d --wait` (healthcheck on `/status.php` and on the cookbook API version endpoint).
4. `reactivecircus/android-emulator-runner@v2`: API 34, `default` target (AOSP, no Chrome, smaller, rootable), `x86_64`, `-no-window -no-audio -dns-server 10.0.2.2` if the dnsmasq option is chosen, AVD cached between runs with `actions/cache` keyed on API level.
5. `script:` runs the `connectedFullDebugAndroidTest` command from §4 with the URLs pointing at `10.0.2.2` and `nextcloud.test`.
6. Upload `app/build/reports/androidTests` and the emulator logcat as artifacts on failure.

Expected wall time: roughly 8 to 12 minutes (AVD boot 2 to 3 min with a cached snapshot, Nextcloud init about 1 min, six tests 3 to 4 min, APK build). Decisions to make here:

- When it runs. Every PR is the safest; the job is independent of the `fastlane` job and only delays the merge signal, not the fast feedback. Alternatives: only on PRs touching `app/src/main/java/.../auth/**`, `core/util/OkHttpClientProvider.kt`, `di/**`, `e2e/**` (via `paths`), or every PR plus a nightly matrix across Nextcloud versions.
- Whether it is a required check. Recommend not required for the first weeks until flake rate is known.

## 6. Code changes the tests need (all small)

| Change | Why | Layer |
|---|---|---|
| `Modifier.testTag` on the login screens' inputs and buttons | stable node lookup, independent of translations | 3 |
| Extract `AppModule.provideOkHttpClient` body into a reusable builder | Layer 2 must test the production interceptor chain, not a lookalike | 2 |
| Pure Kotlin URL validator replacing `Patterns.WEB_URL` (or Robolectric) | `StartScreenViewModel` is untestable on the JVM today | 1 |
| `LoginEndpointResult.loginUrl` as `String` (parse in the ViewModel) | drops the `mockStatic(Uri)` from repository tests | 1, 2 |
| Instrumentation-argument reader for server URLs and credentials | same tests locally and in CI | 3 |
| An `e2e` test package plus Gradle filter | keep the slow tests out of the screenshot run | 3 |

None of these change behaviour. The validator extraction may surface the single-label host gap mentioned in §2; whether to widen validation then is a product decision, not part of this concept.

## 7. Future: Nextcloud single sign-on through the Files app

The Nextcloud Android SSO library (`nextcloud/Android-SingleSignOn`) does not talk to the server during login at all: it asks the installed Nextcloud Files app for an account via an `AccountManager`/content-provider handshake and then proxies every request through that app. Two consequences for this concept:

- It is a third **method**, orthogonal to transport. The transport matrix becomes the Files app's problem; the Cookbook app only needs to prove it stores the account it was handed and that the API provider can build a client from it.
- It cannot be tested on the JVM or against Docker alone: an end-to-end test needs the Files app installed on the emulator and logged in, which means driving the Files app's own login UI with UiAutomator first. That is approach A from §2 with another app's UI on top, and it is not worth automating.

Plan for when it arrives:

1. Put the account acquisition behind a small interface (for example `AccountSource` with `LoginFlowV2AccountSource`, `ManualAccountSource`, `SsoAccountSource`). Layer 1 tests the ViewModel against a fake `AccountSource`; Layer 2 tests the two network-backed sources against `MockWebServer`.
2. For `SsoAccountSource`, a Layer 1 test with a faked `SingleSignOnAccount` result and a Layer 3 test that only checks the "Files app not installed" and "user cancelled the account picker" paths, which need no real Files app. This is where `hilt-android-testing` and a `@TestInstallIn` replacement of the SSO module become necessary.
3. The happy path goes on the Layer 4 checklist.

## 8. Dependencies to add

| Artifact | Configuration | Used by |
|---|---|---|
| `com.squareup.okhttp3:mockwebserver` (BOM-managed) | `testImplementation` | Layer 2 |
| `com.squareup.okhttp3:okhttp-tls` (BOM-managed) | `testImplementation` | Layer 2 |
| `androidx.test.espresso:espresso-intents:3.7.0` | `androidTestImplementation` | Layer 3 (approach B) |
| `org.robolectric:robolectric` | `testImplementation` | only if option 2 in Layer 1 is chosen |
| `com.google.dagger:hilt-android-testing` + `kaptAndroidTest hilt-compiler` | `androidTestImplementation` | deferred to the SSO work |

Plus the non-Gradle pieces: `e2e/docker-compose.yml`, `e2e/Caddyfile`, `e2e/init.sh` (occ setup), optionally `e2e/dnsmasq.conf`, and the new CI job.

## 9. Suggested order

1. **Layer 1 gaps + the URL validator extraction.** Pure JVM, no new infrastructure, immediate value for the manual-login path that has no tests today.
2. **Layer 2.** Adds the two OkHttp test artifacts and the shared client builder. After this step every cell of the matrix has an automated check in the existing CI job.
3. **Docker Compose stack + devbox script**, used manually first. Even without automated tests, a one-command local Nextcloud with Cookbook, http, and self-signed https endpoints removes most of the tedium you described.
4. **Layer 3 with approach B**, scenarios 1, 3, 4 first (no DNS, no self-signed), then 2, 5, 6.
5. **CI job**, non-required until stable.
6. Approach A smoke test and the SSO abstraction when there is a concrete need.

Each step is a self-contained spec in the existing `specs/` format.

## 10. Open decisions

These change what gets built, so they want an answer before the first follow-up spec:

1. **URL validation**: extract a pure validator (and accept that the test table will document the single-label-host rejection), or add Robolectric and keep `Patterns.WEB_URL` as is?
2. **Custom Tab in Layer 3**: approach B only (stubbed browser, HTTP approval) for CI, with A as a local-only extra? Or is a real-browser test in CI worth the flake risk?
3. **Valid https end-to-end**: skip in CI and rely on Layer 2, install a user CA on the emulator, or is there a publicly reachable test instance that a nightly job could use?
4. **Hostname for the domain + port cell**: dnsmasq container + `-dns-server` (self-contained) or nip.io (zero setup, external dependency)?
5. **CI trigger**: every PR, path-filtered, or every PR plus a nightly Nextcloud-version matrix? And should the job be a required check from the start?
6. **Nextcloud version to pin** in the compose file, and whether the Cookbook server app comes from the app store (`occ app:install`) or a pinned release tarball for reproducibility.
7. **Physical devices**: should the local workflow support running Layer 3 against a phone on the LAN from day one (URL arguments + trusted domains from env), or emulator only?
