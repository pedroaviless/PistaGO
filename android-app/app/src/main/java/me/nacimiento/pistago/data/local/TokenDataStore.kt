package me.nacimiento.pistago.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pistago_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val EMAIL_KEY = stringPreferencesKey("user_email")
    private val NOMBRE_KEY = stringPreferencesKey("user_nombre")
    private val ROL_KEY = stringPreferencesKey("user_rol")

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val email: Flow<String?> = context.dataStore.data.map { it[EMAIL_KEY] }
    val nombre: Flow<String?> = context.dataStore.data.map { it[NOMBRE_KEY] }
    val rol: Flow<String?> = context.dataStore.data.map { it[ROL_KEY] }

    suspend fun saveSession(token: String, email: String, nombre: String, rol: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[EMAIL_KEY] = email
            prefs[NOMBRE_KEY] = nombre
            prefs[ROL_KEY] = rol
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}