package com.example.kenyanfoodrecognitionsystem.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val food_name: String?,
    val energy: Float?,
    val fat: Float?,
    val carbohydrates: Float?,
    val protein: Float?,
    val fibre: Float?
)