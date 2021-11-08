package de.lukasneugebauer.nextcloudcookbook.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberImagePainter
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.data.NcAccount
import de.lukasneugebauer.nextcloudcookbook.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.utils.Constants
import de.lukasneugebauer.nextcloudcookbook.utils.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.Credentials
import javax.inject.Inject

@Composable
fun AuthorizedImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier,
    viewModel: AuthorizedImageViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    if (state.account != null) {
        Image(
            painter = rememberImagePainter(
                data = state.account.url + imageUrl,
                builder = {
                    val credentials = Credentials.basic(state.account.username, state.account.token)
                    addHeader("Authorization", credentials)
                    crossfade(Constants.CROSSFADE_DURATION_MILLIS)
                }
            ),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@HiltViewModel
class AuthorizedImageViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _state: MutableState<AuthorizedImageState> = mutableStateOf(AuthorizedImageState())
    val state: State<AuthorizedImageState> = _state

    init {
        viewModelScope.launch {
            accountRepository.getAccount().collect {
                when (it) {
                    is Resource.Success -> _state.value = _state.value.copy(account = it.data)
                    is Resource.Error -> _state.value = _state.value.copy(error = it.text)
                }
            }
        }
    }
}

data class AuthorizedImageState(
    val account: NcAccount? = null,
    val error: String? = null
)