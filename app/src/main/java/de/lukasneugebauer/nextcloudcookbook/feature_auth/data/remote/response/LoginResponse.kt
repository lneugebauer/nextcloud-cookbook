package de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.response

import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginResult

data class LoginResponse(
    val server: String,
    val loginName: String,
    val appPassword: String
) {
    fun toLoginResult(): LoginResult = LoginResult(
        ncAccount = NcAccount(
            name = "",
            username = loginName,
            token = appPassword,
            url = server
        )
    )
}