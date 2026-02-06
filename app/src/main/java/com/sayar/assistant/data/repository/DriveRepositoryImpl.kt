package com.sayar.assistant.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sayar.assistant.data.remote.DriveService
import com.sayar.assistant.domain.model.DriveFile
import com.sayar.assistant.domain.model.UserDriveFolders
import com.sayar.assistant.domain.repository.DriveRepository
import com.sayar.assistant.domain.repository.FolderType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

private val Context.driveDataStore: DataStore<Preferences> by preferencesDataStore(name = "drive_cache")

@Singleton
class DriveRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driveService: DriveService,
    private val json: Json
) : DriveRepository {

    private object PreferencesKeys {
        val USER_FOLDERS = stringPreferencesKey("user_folders")
    }

    override val userFolders: Flow<UserDriveFolders?> = context.driveDataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_FOLDERS]?.let { data ->
            try {
                json.decodeFromString<UserDriveFolders>(data)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun initializeUserFolders(userEmail: String): Result<UserDriveFolders> {
        return try {
            val folderStructure = driveService.createUserFolderStructure(userEmail)

            val userFolders = UserDriveFolders(
                userEmail = userEmail,
                rootFolderId = folderStructure.rootFolder.id,
                timetablesFolderId = folderStructure.timetablesFolder.id,
                studentsFolderId = folderStructure.studentsFolder.id,
                documentsFolderId = folderStructure.documentsFolder.id
            )

            // Cache the folder IDs
            context.driveDataStore.edit { preferences ->
                preferences[PreferencesKeys.USER_FOLDERS] = json.encodeToString(userFolders)
            }

            Result.success(userFolders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkUserFoldersExist(userEmail: String): Boolean {
        return try {
            val folder = driveService.findFolderByName(userEmail)
            folder != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun listFiles(folderId: String): Result<List<DriveFile>> {
        return try {
            val files = driveService.listFiles(folderId)
            val driveFiles = files.map { file ->
                DriveFile(
                    id = file.id,
                    name = file.name,
                    mimeType = file.mimeType ?: "application/octet-stream",
                    size = file.getSize(),
                    createdTime = file.createdTime?.toString(),
                    modifiedTime = file.modifiedTime?.toString(),
                    webViewLink = file.webViewLink
                )
            }
            Result.success(driveFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadFile(
        fileName: String,
        mimeType: String,
        content: ByteArray,
        folderType: FolderType
    ): Result<DriveFile> {
        return try {
            val folders = getCachedFolders()
                ?: return Result.failure(IllegalStateException("User folders not initialized"))

            val folderId = folders.getFolderId(folderType)
            val file = driveService.uploadFile(fileName, mimeType, content, folderId)

            Result.success(
                DriveFile(
                    id = file.id,
                    name = file.name,
                    mimeType = file.mimeType ?: mimeType,
                    size = file.getSize(),
                    webViewLink = file.webViewLink
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadFile(
        fileName: String,
        mimeType: String,
        inputStream: InputStream,
        folderType: FolderType
    ): Result<DriveFile> {
        return try {
            val content = inputStream.readBytes()
            uploadFile(fileName, mimeType, content, folderType)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadFile(fileId: String): Result<ByteArray> {
        return try {
            val content = driveService.downloadFile(fileId)
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFile(fileId: String): Result<Unit> {
        return try {
            driveService.deleteFile(fileId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache() {
        context.driveDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_FOLDERS)
        }
    }

    private suspend fun getCachedFolders(): UserDriveFolders? {
        return context.driveDataStore.data.map { preferences ->
            preferences[PreferencesKeys.USER_FOLDERS]?.let { data ->
                try {
                    json.decodeFromString<UserDriveFolders>(data)
                } catch (e: Exception) {
                    null
                }
            }
        }.let { flow ->
            kotlinx.coroutines.flow.firstOrNull(flow)
        }
    }
}

private suspend fun <T> kotlinx.coroutines.flow.firstOrNull(flow: Flow<T>): T? {
    var result: T? = null
    flow.collect { value ->
        result = value
        return@collect
    }
    return result
}
