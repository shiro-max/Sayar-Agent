package com.sayar.assistant.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayar.assistant.BuildConfig
import com.sayar.assistant.domain.model.User
import com.sayar.assistant.domain.model.UserDriveFolders
import com.sayar.assistant.domain.repository.AuthRepository
import com.sayar.assistant.domain.repository.DriveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val driveRepository: DriveRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSignedIn: StateFlow<Boolean> = authRepository.isSignedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userDriveFolders: StateFlow<UserDriveFolders?> = driveRepository.userFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _driveSetupState = MutableStateFlow<DriveSetupState>(DriveSetupState.Idle)
    val driveSetupState: StateFlow<DriveSetupState> = _driveSetupState.asStateFlow()

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                    // Initialize Drive folders after successful login
                    initializeDriveFolders(user.email)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Sign in failed")
                }
        }
    }

    fun signInAsDemo() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInAsDemo()
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                    // Initialize Drive folders for demo user
                    initializeDriveFolders(user.email)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Demo sign in failed")
                }
        }
    }

    private fun initializeDriveFolders(userEmail: String) {
        if (!BuildConfig.GOOGLE_DRIVE_ENABLED) {
            Log.d(TAG, "Google Drive is disabled, skipping folder initialization")
            _driveSetupState.value = DriveSetupState.Disabled
            return
        }

        viewModelScope.launch {
            _driveSetupState.value = DriveSetupState.Loading
            Log.d(TAG, "Initializing Drive folders for user: $userEmail")

            driveRepository.initializeUserFolders(userEmail)
                .onSuccess { folders ->
                    Log.d(TAG, "Drive folders initialized successfully: ${folders.rootFolderId}")
                    _driveSetupState.value = DriveSetupState.Success(folders)
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to initialize Drive folders", error)
                    _driveSetupState.value = DriveSetupState.Error(
                        error.message ?: "Failed to setup Drive folders"
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            // Clear Drive cache
            driveRepository.clearCache()
            // Sign out
            authRepository.signOut()
            _authState.value = AuthState.Idle
            _driveSetupState.value = DriveSetupState.Idle
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun retryDriveSetup() {
        currentUser.value?.let { user ->
            initializeDriveFolders(user.email)
        }
    }
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class DriveSetupState {
    data object Idle : DriveSetupState()
    data object Disabled : DriveSetupState()
    data object Loading : DriveSetupState()
    data class Success(val folders: UserDriveFolders) : DriveSetupState()
    data class Error(val message: String) : DriveSetupState()
}
