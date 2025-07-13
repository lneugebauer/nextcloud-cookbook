package de.lukasneugebauer.nextcloudcookbook.core.domain.model

data class Preferences(
    val isShowIngredientSyntaxIndicator: Boolean,
    val ncAccount: NcAccount,
    val recipeOfTheDay: RecipeOfTheDay,
    val allowSelfSignedCertificates: Boolean,
)
