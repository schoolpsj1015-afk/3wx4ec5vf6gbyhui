package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    // Favorite Dishes
    @Query("SELECT * FROM favorite_dishes ORDER BY dateAdded DESC")
    fun getAllFavoriteDishes(): Flow<List<FavoriteDishEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteDish(dish: FavoriteDishEntity)

    @Query("DELETE FROM favorite_dishes WHERE dishName = :dishName")
    suspend fun deleteFavoriteDishByName(dishName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_dishes WHERE dishName = :dishName)")
    suspend fun isFavoriteDish(dishName: String): Boolean

    // Meal Ratings
    @Query("SELECT * FROM meal_ratings WHERE dateMealKey = :key LIMIT 1")
    suspend fun getRatingForKey(key: String): MealRatingEntity?

    @Query("SELECT * FROM meal_ratings WHERE dateMealKey = :key LIMIT 1")
    fun getRatingFlowForKey(key: String): Flow<MealRatingEntity?>

    @Query("SELECT * FROM meal_ratings ORDER BY timestamp DESC")
    fun getAllRatings(): Flow<List<MealRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRating(rating: MealRatingEntity)
}
