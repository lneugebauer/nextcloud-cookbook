package de.lukasneugebauer.nextcloudcookbook.core.domain.model

data class Preferences(
    val isShowRecipeSyntaxIndicator: Boolean,
    val ncAccount: NcAccount,
    val recipeOfTheDay: RecipeOfTheDay,
)
