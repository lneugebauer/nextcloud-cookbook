package de.lukasneugebauer.nextcloudcookbook.auth.data.remote.response

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.auth.domain.model.LoginResult
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount

data class LoginResponse(
    @SerializedName("server")
    val server: String,
    @SerializedName("loginName")
    val loginName: String,
    @SerializedName("appPassword")
    val appPassword: String,
) {
    fun toLoginResult(): LoginResult = LoginResult(
        ncAccount = NcAccount(
            name = "",
            username = loginName,
            token = appPassword,
            url = server,
        ),
    )
}
