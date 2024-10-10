package com.example.colorietracker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.colorietracker.PreferencesManager.PreferencesKeys.STORE_LATITUDE
import com.example.colorietracker.PreferencesManager.PreferencesKeys.STORE_LONGITUDE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "userPreferences")

class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    object PreferencesKeys {
        val USER_GOAL = intPreferencesKey("user_goal")
        val MOTIVATION_TOGGLE = booleanPreferencesKey("motivation_toggle")
        val ABILITY_TOGGLE = booleanPreferencesKey("ability_toggle")
        val STORE_LATITUDE = doublePreferencesKey("store_latitude")
        val STORE_LONGITUDE = doublePreferencesKey("store_longitude")
    }

    val getGoal: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_GOAL] ?: 0
        }

    suspend fun saveGoal(goal: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_GOAL] = goal
        }
    }

    val motivationFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.MOTIVATION_TOGGLE] ?: false
        }

    suspend fun saveMotivation(motivation: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MOTIVATION_TOGGLE] = motivation
        }
    }

    val abilityFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ABILITY_TOGGLE] ?: false
        }

    suspend fun saveAbility(ability: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ABILITY_TOGGLE] = ability
        }
    }

    val getStoreLatitude: Flow<Double> = dataStore.data
        .map { preferences ->
            preferences[STORE_LATITUDE] ?: 0.0
        }

    suspend fun saveStoreLatitude(latitude: Double) {
        dataStore.edit { preferences ->
            preferences[STORE_LATITUDE] = latitude
        }
    }

    val getStoreLongitude: Flow<Double> = dataStore.data
        .map { preferences ->
            preferences[STORE_LONGITUDE] ?: 0.0
        }

    suspend fun saveStoreLongitude(longitude: Double) {
        dataStore.edit { preferences ->
            preferences[STORE_LONGITUDE] = longitude
        }
    }
}


