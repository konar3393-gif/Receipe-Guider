package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val ingredients: String, // Newline separated
    val instructions: String, // Newline separated
    val category: String, // e.g. "Breakfast", "Lunch", "Dinner", "Dessert"
    val place: String, // e.g. "Italy", "India", "Mexico", "Japan", "France", "Midnight Fridge"
    val imageUrl: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val isFavorite: Boolean = false,
    val isUserCreated: Boolean = false
)

@Entity(tableName = "timer_presets")
data class TimerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val durationSeconds: Int,
    val category: String = "Kitchen"
)
