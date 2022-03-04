package de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.response

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginResult

data class LoginResponse(
    @SerializedName("server")
    val server: String,
    @SerializedName("loginName")
    val loginName: String,
    @SerializedName("appPassword")
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
