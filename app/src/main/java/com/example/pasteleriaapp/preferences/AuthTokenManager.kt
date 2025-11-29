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

// 1. Crear la instancia de DataStore a nivel de aplicación (fuera de la clase)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

// 2. Crear la clase que interactúa con DataStore
class AuthTokenManager @Inject constructor(private val context: Context) {

    // 🔑 CLAVE: El nombre bajo el cual se guardará el token
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }

    // --- GUARDAR TOKEN ---
    suspend fun saveAuthData(token: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ROLE_KEY] = role
        }
    }

    // --- LEER TOKEN ---
    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }

    // --- BORRAR TOKEN (Logout) ---
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(USER_ROLE_KEY)
        }
    }
}