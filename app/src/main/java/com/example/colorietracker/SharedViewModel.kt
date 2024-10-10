package com.example.colorietracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    private val _calorieGoal = MutableStateFlow(0)
    val calorieGoal: StateFlow<Int> = _calorieGoal.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.getGoal.collect {
                _calorieGoal.value = it
            }
        }
    }

    fun saveGoal(goal: Int) {
        viewModelScope.launch {
            preferencesManager.saveGoal(goal)
        }
    }
}
