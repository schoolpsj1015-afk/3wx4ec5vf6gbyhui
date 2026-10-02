package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_dishes")
data class FavoriteDishEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dishName: String,
    val dateAdded: Long = System.currentTimeMillis()
)
