package com.example.colorietracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorietracker.database.CalorieDao
import com.example.colorietracker.database.CalorieEntry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(
    private val calorieDao: CalorieDao,

) : ViewModel() {
    private val _totalCalories = MutableStateFlow(0)
    val totalCalories: StateFlow<Int> = _totalCalories

    init {
        getTotalCaloriesForToday()
    }

    private fun getTotalCaloriesForToday() {
        viewModelScope.launch {
            calorieDao.getTotalCaloriesForDate(LocalDate.now()).collect { totalCalories ->
                _totalCalories.emit(totalCalories)
            }
        }
    }

    fun addCalories(calories: Int) {
        viewModelScope.launch {
            calorieDao.insertCalorieEntry(CalorieEntry(date = LocalDate.now(), calories = calories))
            getTotalCaloriesForToday()
        }
    }
}
