package com.sayar.assistant.domain.repository

import com.sayar.assistant.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    val isSignedIn: Flow<Boolean>

    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInAsDemo(): Result<User>
    suspend fun signOut()
}
