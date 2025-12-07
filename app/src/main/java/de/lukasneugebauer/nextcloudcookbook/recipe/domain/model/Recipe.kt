package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

import java.time.Duration

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val imageOrigin: String,
    val imageUrl: String,
    val category: String,
    val keywords: List<String>,
    val yield: Int,
    val prepTime: Duration?,
    val cookTime: Duration?,
    val totalTime: Duration?,
    val nutrition: Nutrition?,
    val tools: List<Tool>,
    val ingredients: List<Ingredient>,
    val instructions: List<Instruction>,
    // TODO: 19.08.21 Change createdAt and modifiedAt to date fields.
    val createdAt: String,
    val modifiedAt: String,
) {
    fun isEmpty(): Boolean = this.id == "0"

    fun isNotEmpty(): Boolean = this.id != "0"
}
