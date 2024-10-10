package com.example.colorietracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CalorieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalorieEntry(calorieEntry: CalorieEntry)

    @Query("SELECT COALESCE(SUM(calories), 0) FROM daily_calories WHERE date = :date")
    fun getTotalCaloriesForDate(date: LocalDate): Flow<Int>
}