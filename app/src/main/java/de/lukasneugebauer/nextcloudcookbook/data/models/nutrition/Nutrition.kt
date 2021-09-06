package de.lukasneugebauer.nextcloudcookbook.data.models.nutrition

data class NutritionNw(
    val calories: String?,
    val carbohydrateContent: String?,
    val cholesterolContent: String?,
    val fatContent: String?,
    val fiberContent: String?,
    val proteinContent: String?,
    val saturatedFatContent: String?,
    val servingSize: String?,
    val sodiumContent: String?,
    val sugarContent: String?,
    val transFatContent: String?,
    val unsaturatedFatContent: String?,
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
        unsaturatedFatContent = unsaturatedFatContent,
    )
}

data class Nutrition(
    val calories: String?,
    val carbohydrateContent: String?,
    val cholesterolContent: String?,
    val fatContent: String?,
    val fiberContent: String?,
    val proteinContent: String?,
    val saturatedFatContent: String?,
    val servingSize: String?,
    val sodiumContent: String?,
    val sugarContent: String?,
    val transFatContent: String?,
    val unsaturatedFatContent: String?,
)