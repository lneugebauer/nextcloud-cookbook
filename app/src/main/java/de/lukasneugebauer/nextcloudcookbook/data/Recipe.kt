package de.lukasneugebauer.nextcloudcookbook.data

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
    val prepTime: String,
    val cookTime: String,
    val totalTime: String,
    val nutrition: List<String>,
    val tools: List<String>,
    val ingredients: List<String>,
    val instructions: List<String>,
    // TODO: 19.08.21 Change createdAt and modifiedAt to date fields.
    val createdAt: String,
    val modifiedAt: String,
)