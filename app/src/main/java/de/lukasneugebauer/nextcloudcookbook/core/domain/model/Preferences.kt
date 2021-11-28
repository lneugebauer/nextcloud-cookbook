package de.lukasneugebauer.nextcloudcookbook.core.domain.model

data class Preferences(
    val ncAccount: NcAccount,
    val useSingleSignOn: Boolean,
    val recipeOfTheDay: RecipeOfTheDay
)