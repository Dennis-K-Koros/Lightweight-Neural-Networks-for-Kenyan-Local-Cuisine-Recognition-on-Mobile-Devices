package com.example.kenyanfoodrecognitionsystem.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_history")
data class MealHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Image stored as Base64 string or file path
    val imageData: String,

    // Timestamp of when the meal was classified
    val timestamp: Long,

    // Date string for easy querying (format: "yyyy-MM-dd")
    val dateString: String,

    // Main classified food name
    val classifiedFoodName: String,

    // All food items in the meal (comma-separated or JSON)
    val foodItems: String, // e.g., "Ugali (200g), Sukuma Wiki (100g)"

    // Total nutritional values
    val totalEnergy: Float?,
    val totalProtein: Float?,
    val totalFat: Float?,
    val totalCarbohydrates: Float?,
    val totalFibre: Float?,

    // Meal type (optional: breakfast, lunch, dinner, snack)
    val mealType: String? = null,

    // Notes (optional)
    val notes: String? = null
)