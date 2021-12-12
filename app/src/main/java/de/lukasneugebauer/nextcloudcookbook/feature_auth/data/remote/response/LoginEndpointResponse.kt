package de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.response

import androidx.core.net.toUri
import de.lukasneugebauer.nextcloudcookbook.feature_auth.data.dto.PollDto
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginEndpointResult

data class LoginEndpointResponse(
    val poll: PollDto,
    val login: String
) {
    fun toLoginEndpointResult() = LoginEndpointResult(
        token = poll.token,
        pollUrl = poll.endpoint,
        loginUrl = login.toUri()
    )
}