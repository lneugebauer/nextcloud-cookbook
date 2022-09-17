package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Nutrition

data class NutritionDto(
    @SerializedName("calories")
    val calories: String?,
    @SerializedName("carbohydrateContent")
    val carbohydrateContent: String?,
    @SerializedName("cholesterolContent")
    val cholesterolContent: String?,
    @SerializedName("fatContent")
    val fatContent: String?,
    @SerializedName("fiberContent")
    val fiberContent: String?,
    @SerializedName("proteinContent")
    val proteinContent: String?,
    @SerializedName("saturatedFatContent")
    val saturatedFatContent: String?,
    @SerializedName("servingSize")
    val servingSize: String?,
    @SerializedName("sodiumContent")
    val sodiumContent: String?,
    @SerializedName("sugarContent")
    val sugarContent: String?,
    @SerializedName("transFatContent")
    val transFatContent: String?,
    @SerializedName("unsaturatedFatContent")
    val unsaturatedFatContent: String?
) {
    fun toNutrition() = Nutrition(
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
        unsaturatedFatContent = unsaturatedFatContent
    )
}
