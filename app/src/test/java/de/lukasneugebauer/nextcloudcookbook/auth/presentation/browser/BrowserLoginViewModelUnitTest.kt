package de.lukasneugebauer.nextcloudcookbook.auth.presentation.browser

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.model.LoginEndpointResult
import de.lukasneugebauer.nextcloudcookbook.auth.domain.model.LoginResult
import de.lukasneugebauer.nextcloudcookbook.auth.domain.repository.AuthRepository
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.BrowserLoginScreenState
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApi
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

/**
 * Unit tests for [BrowserLoginViewModel].
 *
 * The ViewModel drives the Login Flow v2 handshake while the login page itself lives in a Custom
 * Tab, so what matters here is that the tab is opened exactly once per `Loaded` state and that
 * polling keeps running only while that state holds.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowserLoginViewModelUnitTest {
    private lateinit var accountRepository: AccountRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var clearPreferencesUseCase: ClearPreferencesUseCase
    private lateinit var ncCookbookApiProvider: NcCookbookApiProvider
    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())

        accountRepository = mock()
        authRepository = mock()
        clearPreferencesUseCase = mock()
        ncCookbookApiProvider = mock()
        preferencesManager = mock()

        runBlocking {
            // Never emits a successful account, so observeAuthorizationStatus() stays inert.
            whenever(accountRepository.getAccount()).thenReturn(
                flowOf(Resource.Error(UiText.DynamicString("Not signed in"))),
            )
            whenever(ncCookbookApiProvider.apiFlow).thenReturn(MutableStateFlow<NcCookbookApi?>(null))
            whenever(authRepository.getLoginEndpoint(any())).thenReturn(Resource.Success(LOGIN_ENDPOINT))
            // Succeeding on the first poll keeps the recursion from running forever under
            // advanceUntilIdle(); the two polling tests override it.
            whenever(authRepository.tryLogin(any(), any())).thenReturn(Resource.Success(LOGIN_RESULT))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_withValidUrl_requestsLoginEndpointAndEntersLoaded() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            verify(authRepository).getLoginEndpoint(URL)
            val state = viewModel.uiState.value
            assertTrue(state is BrowserLoginScreenState.Loaded)
            assertEquals(LOGIN_URI, (state as BrowserLoginScreenState.Loaded).loginUrl)
            assertFalse(state.browserLaunched)
        }

    @Test
    fun init_withoutUrl_entersErrorState() =
        runTest {
            val viewModel = createViewModel(SavedStateHandle(emptyMap()))
            advanceUntilIdle()

            assertErrorState(viewModel.uiState.value, R.string.error_invalid_url)
            verifyNoInteractions(authRepository)
        }

    @Test
    fun init_withSavedPollToken_resumesPollingWithoutNewEndpointRequest() =
        runTest {
            // `String.toUri()` is `Uri.parse()`, an Android framework call with no JVM implementation.
            mockStatic(Uri::class.java).use { uri ->
                uri.`when`<Uri> { Uri.parse(SAVED_LOGIN_URL) }.thenReturn(LOGIN_URI)

                val viewModel =
                    createViewModel(
                        SavedStateHandle(
                            mapOf(
                                "url" to URL,
                                "pollUrl" to SAVED_POLL_URL,
                                "pollToken" to SAVED_TOKEN,
                                "loginUrl" to SAVED_LOGIN_URL,
                                "browserLaunched" to true,
                            ),
                        ),
                    )
                advanceUntilIdle()

                verify(authRepository, never()).getLoginEndpoint(any())
                verify(authRepository).tryLogin(SAVED_POLL_URL, SAVED_TOKEN)
                val state = viewModel.uiState.value
                assertTrue(state is BrowserLoginScreenState.Loaded)
                // Restored as launched, so the screen's collector does not re-open the tab.
                assertTrue((state as BrowserLoginScreenState.Loaded).browserLaunched)
            }
        }

    @Test
    fun getLoginEndpoint_onSuccess_persistsPollTokenToSavedStateHandle() =
        runTest {
            val savedStateHandle = SavedStateHandle(mapOf("url" to URL))

            createViewModel(savedStateHandle)
            advanceUntilIdle()

            assertEquals(TOKEN, savedStateHandle.get<String>("pollToken"))
            assertEquals(POLL_URL, savedStateHandle.get<String>("pollUrl"))
            assertEquals(LOGIN_URI.toString(), savedStateHandle.get<String>("loginUrl"))
        }

    @Test
    fun onBrowserLaunched_setsBrowserLaunchedFlag() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onBrowserLaunched()

            val state = viewModel.uiState.value
            assertTrue(state is BrowserLoginScreenState.Loaded)
            assertTrue((state as BrowserLoginScreenState.Loaded).browserLaunched)
        }

    @Test
    fun onOpenBrowserClick_clearsBrowserLaunchedFlag() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onBrowserLaunched()
            viewModel.onOpenBrowserClick()

            val state = viewModel.uiState.value
            assertTrue(state is BrowserLoginScreenState.Loaded)
            assertFalse((state as BrowserLoginScreenState.Loaded).browserLaunched)
        }

    @Test
    fun onNoBrowserAvailable_entersErrorState() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onNoBrowserAvailable()

            assertErrorState(viewModel.uiState.value, R.string.error_no_browser)
        }

    @Test
    fun pollLoginServer_onError_retriesAfterPollDelay() =
        runTest {
            whenever(authRepository.tryLogin(any(), any())).thenReturn(
                Resource.Error(UiText.DynamicString("Not yet")),
                Resource.Success(LOGIN_RESULT),
            )

            createViewModel()
            runCurrent()
            advanceTimeBy(BrowserLoginViewModel.POLL_DELAY + 1)
            runCurrent()

            verify(authRepository, times(2)).tryLogin(POLL_URL, TOKEN)
            verify(preferencesManager).updateNextcloudAccount(any())
            verify(ncCookbookApiProvider).initApi()
        }

    /** The `is Loaded` guard replaced `pollLoginServerIsActive`; it still has to stop the recursion. */
    @Test
    fun pollLoginServer_stopsWhenStateLeavesLoaded() =
        runTest {
            whenever(authRepository.tryLogin(any(), any())).thenReturn(
                Resource.Error(UiText.DynamicString("Not yet")),
            )

            val viewModel = createViewModel()
            runCurrent()

            viewModel.onNoBrowserAvailable()
            advanceTimeBy(BrowserLoginViewModel.POLL_DELAY * 3)
            runCurrent()

            verify(authRepository, times(1)).tryLogin(POLL_URL, TOKEN)
        }

    @Test
    fun retry_clearsSavedPollTokenAndRequestsNewEndpoint() =
        runTest {
            val savedStateHandle = SavedStateHandle(mapOf("url" to URL))
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            viewModel.retry()

            // Cleared synchronously, otherwise a restart mid-retry would resume the dead token.
            assertNull(savedStateHandle.get<String>("pollToken"))
            assertNull(savedStateHandle.get<String>("pollUrl"))
            assertNull(savedStateHandle.get<String>("loginUrl"))
            advanceUntilIdle()
            verify(authRepository, times(2)).getLoginEndpoint(URL)
        }

    @Test
    fun onBrowserLaunched_persistsFlagToSavedStateHandle() =
        runTest {
            val savedStateHandle = SavedStateHandle(mapOf("url" to URL))
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            viewModel.onBrowserLaunched()

            assertEquals(true, savedStateHandle.get<Boolean>("browserLaunched"))

            viewModel.onOpenBrowserClick()

            assertEquals(false, savedStateHandle.get<Boolean>("browserLaunched"))
        }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("url" to URL))) =
        BrowserLoginViewModel(
            accountRepository = accountRepository,
            authRepository = authRepository,
            clearPreferencesUseCase = clearPreferencesUseCase,
            ncCookbookApiProvider = ncCookbookApiProvider,
            preferencesManager = preferencesManager,
            savedStateHandle = savedStateHandle,
        )

    /** [UiText.StringResource] is not a data class, so compare the resource id. */
    private fun assertErrorState(
        state: BrowserLoginScreenState,
        expectedResId: Int,
    ) {
        assertTrue(state is BrowserLoginScreenState.Error)
        val uiText = (state as BrowserLoginScreenState.Error).uiText
        assertTrue(uiText is UiText.StringResource)
        assertEquals(expectedResId, (uiText as UiText.StringResource).resId)
    }

    companion object {
        private const val URL = "https://cloud.example.tld"
        private const val POLL_URL = "https://cloud.example.tld/index.php/login/v2/poll"
        private const val TOKEN = "token-1"

        /** A poll token minted before the process was killed, distinct from the fresh-request one. */
        private const val SAVED_POLL_URL = "https://cloud.example.tld/index.php/login/v2/poll/saved"
        private const val SAVED_TOKEN = "token-saved"
        private const val SAVED_LOGIN_URL = "https://cloud.example.tld/index.php/login/v2/flow/saved"

        /** `Uri` is an Android framework type, so mock it rather than calling `Uri.parse` on the JVM. */
        private val LOGIN_URI: Uri =
            mock<Uri>().apply {
                whenever(toString()).thenReturn("https://cloud.example.tld/index.php/login/v2/flow/abc")
            }

        private val LOGIN_ENDPOINT =
            LoginEndpointResult(
                token = TOKEN,
                pollUrl = POLL_URL,
                loginUrl = LOGIN_URI,
            )

        private val LOGIN_RESULT =
            LoginResult(
                ncAccount =
                    NcAccount(
                        name = "Test",
                        username = "test",
                        token = "app-password",
                        url = URL,
                    ),
            )
    }
}
