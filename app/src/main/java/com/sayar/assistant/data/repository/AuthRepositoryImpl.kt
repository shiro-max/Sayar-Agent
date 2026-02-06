package com.sayar.assistant.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sayar.assistant.domain.model.User
import com.sayar.assistant.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) : AuthRepository {

    private object PreferencesKeys {
        val USER_DATA = stringPreferencesKey("user_data")
        val ID_TOKEN = stringPreferencesKey("id_token")
    }

    override val currentUser: Flow<User?> = context.authDataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_DATA]?.let { userData ->
            try {
                json.decodeFromString<UserData>(userData).toUser()
            } catch (e: Exception) {
                null
            }
        }
    }

    override val isSignedIn: Flow<Boolean> = currentUser.map { it != null }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            // Decode the ID token to get user info
            // In production, you would validate this token with your backend
            val parts = idToken.split(".")
            if (parts.size >= 2) {
                val payload = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE)
                val payloadJson = String(payload, Charsets.UTF_8)
                val tokenData = json.decodeFromString<TokenPayload>(payloadJson)

                val user = User(
                    id = tokenData.sub,
                    email = tokenData.email,
                    displayName = tokenData.name,
                    photoUrl = tokenData.picture
                )

                // Save user data
                context.authDataStore.edit { preferences ->
                    preferences[PreferencesKeys.USER_DATA] = json.encodeToString(UserData.fromUser(user))
                    preferences[PreferencesKeys.ID_TOKEN] = idToken
                }

                Result.success(user)
            } else {
                Result.failure(IllegalArgumentException("Invalid ID token format"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAsDemo(): Result<User> {
        return try {
            val demoUser = User(
                id = "demo_user",
                email = "demo@sayar.app",
                displayName = "Demo Teacher",
                photoUrl = null
            )

            context.authDataStore.edit { preferences ->
                preferences[PreferencesKeys.USER_DATA] = json.encodeToString(UserData.fromUser(demoUser))
                preferences[PreferencesKeys.ID_TOKEN] = "demo_token"
            }

            Result.success(demoUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        context.authDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_DATA)
            preferences.remove(PreferencesKeys.ID_TOKEN)
        }
    }
}

@kotlinx.serialization.Serializable
private data class UserData(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?
) {
    fun toUser() = User(id, email, displayName, photoUrl)

    companion object {
        fun fromUser(user: User) = UserData(user.id, user.email, user.displayName, user.photoUrl)
    }
}

@kotlinx.serialization.Serializable
private data class TokenPayload(
    val sub: String,
    val email: String,
    val name: String? = null,
    val picture: String? = null
)
