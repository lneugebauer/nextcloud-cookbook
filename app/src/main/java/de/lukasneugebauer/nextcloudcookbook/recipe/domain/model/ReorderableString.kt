package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

interface ReorderableString {
    val id: Int
    val value: String
}

data class Tool(
    override val id: Int,
    override val value: String,
) : ReorderableString

data class Ingredient(
    override val id: Int,
    override val value: String,
    val hasCorrectSyntax: Boolean,
) : ReorderableString

data class Instruction(
    override val id: Int,
    override val value: String,
) : ReorderableString
