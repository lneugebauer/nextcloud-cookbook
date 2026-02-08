package de.lukasneugebauer.nextcloudcookbook.core.domain.model

import androidx.compose.runtime.compositionLocalOf

data class Preferences(
    val isShowIngredientSyntaxIndicator: Boolean,
    val ncAccount: NcAccount,
    val recipeOfTheDay: RecipeOfTheDay,
    val allowSelfSignedCertificates: Boolean,
)

val LocalPreferences = compositionLocalOf<Preferences?> { null }
