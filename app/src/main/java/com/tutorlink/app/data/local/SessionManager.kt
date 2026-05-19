package com.tutorlink.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "tutorlink_session")

/**
 * FIXED: Stores BOTH userId (users table) AND profileId (students/tutors table).
 *
 * The backend has separate tables:
 *   users      → Usuario.id   → returned in AuthResponse.userId
 *   students   → Estudiante.id → needed for /api/sessions/student/{studentId}
 *   tutors     → Tutor.id     → needed for /api/sessions/tutor/{tutorId}
 *
 * After login we fetch the profile and save its ID separately as profileId.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val AUTH_TOKEN  = stringPreferencesKey("auth_token")
        val USER_ROLE   = stringPreferencesKey("user_role")
        val USER_NAME   = stringPreferencesKey("user_name")
        val USER_ID     = longPreferencesKey("user_id")
        val PROFILE_ID  = longPreferencesKey("profile_id")
        val USER_EMAIL  = stringPreferencesKey("user_email")
    }

    private val dataStore = context.dataStore

    val authToken: Flow<String?> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[AUTH_TOKEN] }

    val userRole: Flow<String?> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[USER_ROLE] }

    val userName: Flow<String?> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[USER_NAME] }

    /** ID from 'users' table — what AuthResponse.userId returns */
    val userId: Flow<Long?> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[USER_ID] }

    /**
     * ID from 'students' or 'tutors' table.
     * Pass this to session endpoints, NOT userId.
     * Null until profile is fetched after login.
     */
    val profileId: Flow<Long?> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[PROFILE_ID] }

    val userEmail: Flow<String?> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[USER_EMAIL] }

    suspend fun saveSession(
        token: String,
        role: String,
        name: String,
        userId: Long,
        email: String = ""
    ) {
        dataStore.edit { prefs ->
            prefs[AUTH_TOKEN] = token
            prefs[USER_ROLE]  = role
            prefs[USER_NAME]  = name
            prefs[USER_ID]    = userId
            prefs[USER_EMAIL] = email
        }
    }

    /** Call this after fetching the student/tutor profile from the API */
    suspend fun saveProfileId(id: Long) {
        dataStore.edit { prefs -> prefs[PROFILE_ID] = id }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}