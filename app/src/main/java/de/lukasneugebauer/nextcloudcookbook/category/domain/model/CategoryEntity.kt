package de.lukasneugebauer.nextcloudcookbook.category.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String,
    val recipeCount: Int,
)