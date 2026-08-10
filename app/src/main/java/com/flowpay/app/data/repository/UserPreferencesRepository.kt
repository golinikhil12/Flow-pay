package com.flowpay.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flowpay_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_ACTIVE_USER_ID = stringPreferencesKey("active_user_id")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_OFFLINE_SIMULATOR = booleanPreferencesKey("offline_simulator")
        val KEY_BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
    }

    val activeUserIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACTIVE_USER_ID] ?: "user_1"
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: true
    }

    val isOfflineModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_OFFLINE_SIMULATOR] ?: false
    }

    val isBiometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BIOMETRIC_LOCK] ?: false
    }

    suspend fun setActiveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_USER_ID] = userId
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setOfflineMode(offline: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_OFFLINE_SIMULATOR] = offline
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_LOCK] = enabled
        }
    }
}
