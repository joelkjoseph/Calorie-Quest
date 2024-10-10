package com.example.colorietracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.colorietracker.database.CalorieDao


class MainViewModelFactory(
    private val calorieDao: CalorieDao,


) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(calorieDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
