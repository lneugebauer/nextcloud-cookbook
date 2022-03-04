package de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model

import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount

data class LoginResult(
    val ncAccount: NcAccount
)
