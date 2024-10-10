package com.example.colorietracker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SettingsViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    val userGoal: Flow<Int> = preferencesManager.getGoal
    val motivationEnabled: Flow<Boolean> = preferencesManager.motivationFlow
    val abilityEnabled: Flow<Boolean> = preferencesManager.abilityFlow
    val storeLatitude: Flow<Double> = preferencesManager.getStoreLatitude
    val storeLongitude: Flow<Double> = preferencesManager.getStoreLongitude


    fun saveMotivation(isEnabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveMotivation(isEnabled)
        }
    }

    fun saveAbility(isEnabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveAbility(isEnabled)
        }
    }

    fun saveStoreLatitude(latitude: Double) {
        viewModelScope.launch {
            preferencesManager.saveStoreLatitude(latitude)
        }
    }

    fun saveStoreLongitude(longitude: Double) {
        viewModelScope.launch {
            preferencesManager.saveStoreLongitude(longitude)
        }
    }

}
