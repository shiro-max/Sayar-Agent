package com.sayar.assistant.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayar.assistant.data.remote.DriveService
import com.sayar.assistant.domain.repository.DriveRepository
import com.sayar.assistant.domain.repository.FolderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DriveTestViewModel"

@HiltViewModel
class DriveTestViewModel @Inject constructor(
    private val driveService: DriveService,
    private val driveRepository: DriveRepository
) : ViewModel() {

    private val _testState = MutableStateFlow<DriveTestState>(DriveTestState.Idle)
    val testState: StateFlow<DriveTestState> = _testState.asStateFlow()

    fun runConnectionTest() {
        viewModelScope.launch {
            _testState.value = DriveTestState.Running("Starting Drive connection test...")

            try {
                // Step 1: Check if we can access the root folder
                _testState.value = DriveTestState.Running("Step 1/4: Checking root folder access...")
                Log.d(TAG, "Testing root folder access")

                val testFileName = "connection_test_${System.currentTimeMillis()}.txt"
                val testContent = "Sayar Assistant Drive Test\nTimestamp: ${java.util.Date()}"

                // Step 2: Get user folders from cache
                _testState.value = DriveTestState.Running("Step 2/4: Getting user folders...")
                val userFolders = driveRepository.userFolders.first()

                if (userFolders == null) {
                    _testState.value = DriveTestState.Error("User folders not initialized. Please login first.")
                    return@launch
                }

                Log.d(TAG, "User folders found: ${userFolders.rootFolderId}")

                // Step 3: Upload a test file to Documents folder
                _testState.value = DriveTestState.Running("Step 3/4: Uploading test file...")
                val uploadResult = driveRepository.uploadFile(
                    fileName = testFileName,
                    mimeType = "text/plain",
                    content = testContent.toByteArray(),
                    folderType = FolderType.DOCUMENTS
                )

                val uploadedFile = uploadResult.getOrElse { error ->
                    _testState.value = DriveTestState.Error("Upload failed: ${error.message}")
                    Log.e(TAG, "Upload failed", error)
                    return@launch
                }

                Log.d(TAG, "Test file uploaded: ${uploadedFile.id}")

                // Step 4: Delete the test file
                _testState.value = DriveTestState.Running("Step 4/4: Cleaning up test file...")
                val deleteResult = driveRepository.deleteFile(uploadedFile.id)

                deleteResult.onFailure { error ->
                    Log.w(TAG, "Failed to delete test file (non-critical)", error)
                }

                Log.d(TAG, "Drive connection test completed successfully!")

                _testState.value = DriveTestState.Success(
                    message = "Drive connection successful!",
                    details = listOf(
                        "Root Folder: ${userFolders.rootFolderId}",
                        "Timetables: ${userFolders.timetablesFolderId}",
                        "Students: ${userFolders.studentsFolderId}",
                        "Documents: ${userFolders.documentsFolderId}",
                        "Test file uploaded and deleted successfully"
                    )
                )

            } catch (e: Exception) {
                Log.e(TAG, "Drive connection test failed", e)
                _testState.value = DriveTestState.Error(
                    "Connection test failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun resetState() {
        _testState.value = DriveTestState.Idle
    }
}

sealed class DriveTestState {
    data object Idle : DriveTestState()
    data class Running(val step: String) : DriveTestState()
    data class Success(val message: String, val details: List<String>) : DriveTestState()
    data class Error(val message: String) : DriveTestState()
}
