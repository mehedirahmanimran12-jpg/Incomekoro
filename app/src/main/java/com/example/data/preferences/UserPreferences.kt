package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "income_koro_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val SAVED_USER_ID = stringPreferencesKey("saved_user_id")
        private val SAVED_USER_EMAIL = stringPreferencesKey("saved_user_email")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val savedUserId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SAVED_USER_ID]
    }

    val savedUserEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SAVED_USER_EMAIL]
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveUserSession(userId: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_USER_ID] = userId
            preferences[SAVED_USER_EMAIL] = email
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(SAVED_USER_ID)
            preferences.remove(SAVED_USER_EMAIL)
        }
    }
}
