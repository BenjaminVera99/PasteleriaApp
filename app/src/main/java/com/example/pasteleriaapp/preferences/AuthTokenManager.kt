package com.example.pasteleriaapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class AuthTokenManager @Inject constructor(private val context: Context) {

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    suspend fun saveAuthData(token: String, role: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ROLE_KEY] = role
            preferences[USER_EMAIL_KEY] = email
        }
    }

    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL_KEY]
        }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(USER_ROLE_KEY)
            preferences.remove(USER_EMAIL_KEY)
        }
    }
}