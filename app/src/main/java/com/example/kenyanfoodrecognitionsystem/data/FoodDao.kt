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
}