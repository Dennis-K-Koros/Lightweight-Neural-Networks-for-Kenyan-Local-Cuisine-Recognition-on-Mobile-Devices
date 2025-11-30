package com.example.kenyanfoodrecognitionsystem.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealHistoryDao {

    // Insert a new meal history record
    @Insert
    suspend fun insertMeal(mealHistory: MealHistory): Long

    // Update an existing meal history record
    @Update
    suspend fun updateMeal(mealHistory: MealHistory)

    // Delete a meal history record
    @Delete
    suspend fun deleteMeal(mealHistory: MealHistory)

    // Get all meal history records, ordered by timestamp (newest first)
    @Query("SELECT * FROM meal_history ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealHistory>>

    // Get meals for a specific date
    @Query("SELECT * FROM meal_history WHERE dateString = :date ORDER BY timestamp DESC")
    suspend fun getMealsByDate(date: String): List<MealHistory>

    // Get meals within a date range
    @Query("SELECT * FROM meal_history WHERE dateString BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    suspend fun getMealsByDateRange(startDate: String, endDate: String): List<MealHistory>

    // Get a specific meal by ID
    @Query("SELECT * FROM meal_history WHERE id = :mealId")
    suspend fun getMealById(mealId: Int): MealHistory?

    // Get total calories consumed for a specific date
    @Query("SELECT SUM(totalEnergy) FROM meal_history WHERE dateString = :date")
    suspend fun getTotalCaloriesForDate(date: String): Float?

    // Get total nutrients for a specific date
    @Query("SELECT SUM(totalEnergy) as energy, SUM(totalProtein) as protein, SUM(totalFat) as fat, SUM(totalCarbohydrates) as carbs, SUM(totalFibre) as fibre FROM meal_history WHERE dateString = :date")
    suspend fun getTotalNutrientsForDate(date: String): DailyNutrientSummary?

    // Delete all meal history (for testing/reset)
    @Query("DELETE FROM meal_history")
    suspend fun deleteAllMeals()

    // Get recent meals (last N meals)
    @Query("SELECT * FROM meal_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMeals(limit: Int): List<MealHistory>

    // Search meals by food name
    @Query("SELECT * FROM meal_history WHERE classifiedFoodName LIKE '%' || :query || '%' OR foodItems LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMeals(query: String): List<MealHistory>
}

// Data class for daily nutrient summary
data class DailyNutrientSummary(
    val energy: Float?,
    val protein: Float?,
    val fat: Float?,
    val carbs: Float?,
    val fibre: Float?
)