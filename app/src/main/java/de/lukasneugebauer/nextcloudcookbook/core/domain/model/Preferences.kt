package de.lukasneugebauer.nextcloudcookbook.core.domain.model

import androidx.compose.runtime.compositionLocalOf
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants

data class Preferences(
    val isShowIngredientSyntaxIndicator: Boolean,
    val ncAccount: NcAccount,
    val recipeOfTheDay: RecipeOfTheDay,
    val allowSelfSignedCertificates: Boolean,
    val recipeImageUploadFolder: String = Constants.DEFAULT_RECIPE_IMAGE_UPLOAD_FOLDER,
)

val LocalPreferences = compositionLocalOf<Preferences?> { null }
