package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_ratings")
data class MealRatingEntity(
    @PrimaryKey
    val dateMealKey: String, // e.g. "20261002_2"
    val date: String,        // "20261002"
    val mealCode: String,    // "2"
    val mealName: String,    // "중식"
    val rating: Float,       // 1.0 to 5.0
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
