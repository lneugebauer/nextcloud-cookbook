package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Nutrition

data class NutritionDto(
    @SerializedName("calories")
    val calories: String? = null,
    @SerializedName("carbohydrateContent")
    val carbohydrateContent: String? = null,
    @SerializedName("cholesterolContent")
    val cholesterolContent: String? = null,
    @SerializedName("fatContent")
    val fatContent: String? = null,
    @SerializedName("fiberContent")
    val fiberContent: String? = null,
    @SerializedName("proteinContent")
    val proteinContent: String? = null,
    @SerializedName("saturatedFatContent")
    val saturatedFatContent: String? = null,
    @SerializedName("servingSize")
    val servingSize: String? = null,
    @SerializedName("sodiumContent")
    val sodiumContent: String? = null,
    @SerializedName("sugarContent")
    val sugarContent: String? = null,
    @SerializedName("transFatContent")
    val transFatContent: String? = null,
    @SerializedName("unsaturatedFatContent")
    val unsaturatedFatContent: String? = null,
) {
    fun toNutrition(): Nutrition? {
        if (calories == null &&
            carbohydrateContent == null &&
            cholesterolContent == null &&
            fatContent == null &&
            fiberContent == null &&
            proteinContent == null &&
            saturatedFatContent == null &&
            servingSize == null &&
            sodiumContent == null &&
            sugarContent == null &&
            transFatContent == null &&
            unsaturatedFatContent == null
        ) {
            return null
        }

        return Nutrition(
            calories = calories,
            carbohydrateContent = carbohydrateContent,
            cholesterolContent = cholesterolContent,
            fatContent = fatContent,
            fiberContent = fiberContent,
            proteinContent = proteinContent,
            saturatedFatContent = saturatedFatContent,
            servingSize = servingSize,
            sodiumContent = sodiumContent,
            sugarContent = sugarContent,
            transFatContent = transFatContent,
            unsaturatedFatContent = unsaturatedFatContent,
        )
    }
}
