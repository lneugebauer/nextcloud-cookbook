package de.lukasneugebauer.nextcloudcookbook.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextcloud.android.sso.model.SingleSignOnAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.data.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    fun storeNcSingleSignOnAccount(ncSsoAccount: SingleSignOnAccount) {
        val ncAccount = NcAccount(
            name = ncSsoAccount.name,
            username = ncSsoAccount.userId,
            token = ncSsoAccount.token,
            url = ncSsoAccount.url
        )
        viewModelScope.launch {
            preferencesManager.updateNextcloudAccount(ncAccount)
            preferencesManager.updateUseSingleSignOn(true)
        }
    }
}