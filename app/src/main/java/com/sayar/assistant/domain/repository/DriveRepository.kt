package com.sayar.assistant.domain.repository

import com.sayar.assistant.domain.model.DriveFile
import com.sayar.assistant.domain.model.UserDriveFolders
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface DriveRepository {
    /**
     * Initialize user's folder structure on first login
     * Creates: UserEmail/Timetables/, UserEmail/Students/, UserEmail/Documents/
     */
    suspend fun initializeUserFolders(userEmail: String): Result<UserDriveFolders>

    /**
     * Get cached user folder IDs
     */
    val userFolders: Flow<UserDriveFolders?>

    /**
     * Check if user folders exist
     */
    suspend fun checkUserFoldersExist(userEmail: String): Boolean

    /**
     * List files in a specific folder
     */
    suspend fun listFiles(folderId: String): Result<List<DriveFile>>

    /**
     * Upload a file
     */
    suspend fun uploadFile(
        fileName: String,
        mimeType: String,
        content: ByteArray,
        folderType: FolderType
    ): Result<DriveFile>

    /**
     * Upload a file from InputStream
     */
    suspend fun uploadFile(
        fileName: String,
        mimeType: String,
        inputStream: InputStream,
        folderType: FolderType
    ): Result<DriveFile>

    /**
     * Download a file
     */
    suspend fun downloadFile(fileId: String): Result<ByteArray>

    /**
     * Delete a file
     */
    suspend fun deleteFile(fileId: String): Result<Unit>

    /**
     * Clear cached folder data (on logout)
     */
    suspend fun clearCache()

    /**
     * Save user profile to Drive
     */
    suspend fun saveUserProfile(userJson: String): Result<DriveFile>

    /**
     * Load user profile from Drive
     */
    suspend fun loadUserProfile(): Result<String?>

    /**
     * Save chat history to Drive
     */
    suspend fun saveChatHistory(chatJson: String): Result<DriveFile>

    /**
     * Load chat history from Drive
     */
    suspend fun loadChatHistory(): Result<String?>
}

enum class FolderType {
    ROOT,
    TIMETABLES,
    STUDENTS,
    DOCUMENTS
}
