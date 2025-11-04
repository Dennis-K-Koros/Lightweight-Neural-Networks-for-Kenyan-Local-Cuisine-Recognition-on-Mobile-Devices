package com.example.kenyanfoodrecognitionsystem.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FoodDao {

    @Query("SELECT * FROM foods WHERE LOWER(food_name) = LOWER(:name) LIMIT 1")
    suspend fun getFoodByName(name: String): Food?

    // Fallback: try partial match
    @Query("SELECT * FROM foods WHERE LOWER(food_name) LIKE '%' || LOWER(:name) || '%' LIMIT 1")
    suspend fun getSimilarFoodByName(name: String): Food?

    // New: Search foods by partial name match for autocomplete
    @Query("SELECT * FROM foods WHERE LOWER(food_name) LIKE '%' || LOWER(:query) || '%' LIMIT 10")
    suspend fun searchFoodsByName(query: String): List<Food>

    // New: Get all food names for suggestions
    @Query("SELECT food_name FROM foods ORDER BY food_name ASC")
    suspend fun getAllFoodNames(): List<String?>

}