package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model

import java.time.Duration

data class Recipe(
    val id: Int,
    val name: String,
    val description: String,
    val url: String,
    val imageUrl: String,
    val category: String,
    val keywords: List<String>,
    val yield: Int,
    // TODO: 19.08.21 Change prepTime, cookTime and totalTime to time fields.
    val prepTime: Duration?,
    val cookTime: Duration?,
    val totalTime: Duration?,
    val nutrition: Nutrition?,
    val tools: List<String>,
    val ingredients: List<String>,
    val instructions: List<String>,
    // TODO: 19.08.21 Change createdAt and modifiedAt to date fields.
    val createdAt: String,
    val modifiedAt: String,
) {
    fun isEmpty() : Boolean {
        return this.id == 0
    }

    fun isNotEmpty() : Boolean {
        return this.id != 0
    }
}