package com.example.colorietracker


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.example.colorietracker.database.CalorieDao
import com.example.colorietracker.database.CalorieEntry
import com.example.colorietracker.database.LocalDateConverter


@Database(entities = [CalorieEntry::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calorieDao(): CalorieDao
}
