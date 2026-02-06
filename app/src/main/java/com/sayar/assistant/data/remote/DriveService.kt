package com.sayar.assistant.data.remote

import android.content.Context
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.sayar.assistant.BuildConfig
import com.sayar.assistant.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val driveService: Drive by lazy {
        createDriveService()
    }

    private val rootFolderId: String = BuildConfig.GOOGLE_DRIVE_ROOT_FOLDER_ID

    private fun createDriveService(): Drive {
        val credentials = loadServiceAccountCredentials()
        val httpTransport = NetHttpTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        return Drive.Builder(
            httpTransport,
            jsonFactory,
            HttpCredentialsAdapter(credentials)
        )
            .setApplicationName("Sayar Assistant")
            .build()
    }

    private fun loadServiceAccountCredentials(): GoogleCredentials {
        // Load from raw resource file
        val inputStream: InputStream = context.resources.openRawResource(R.raw.service_account_key)
        return GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf(DriveScopes.DRIVE))
    }

    /**
     * Find a folder by name within a parent folder
     */
    suspend fun findFolderByName(folderName: String, parentFolderId: String = rootFolderId): File? =
        withContext(Dispatchers.IO) {
            val query = "name = '$folderName' and '$parentFolderId' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
            val result: FileList = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name, createdTime, modifiedTime)")
                .execute()

            result.files.firstOrNull()
        }

    /**
     * Create a folder in the specified parent folder
     */
    suspend fun createFolder(folderName: String, parentFolderId: String = rootFolderId): File =
        withContext(Dispatchers.IO) {
            val folderMetadata = File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
                parents = listOf(parentFolderId)
            }

            driveService.files().create(folderMetadata)
                .setFields("id, name, createdTime, webViewLink")
                .execute()
        }

    /**
     * Get or create a folder - returns existing folder if found, creates new one if not
     */
    suspend fun getOrCreateFolder(folderName: String, parentFolderId: String = rootFolderId): File {
        val existingFolder = findFolderByName(folderName, parentFolderId)
        return existingFolder ?: createFolder(folderName, parentFolderId)
    }

    /**
     * Create the standard folder structure for a user
     * Structure: UserEmail/
     *              ├── Timetables/
     *              ├── Students/
     *              └── Documents/
     */
    suspend fun createUserFolderStructure(userEmail: String): UserFolderStructure =
        withContext(Dispatchers.IO) {
            // Create or get user's root folder
            val userFolder = getOrCreateFolder(userEmail, rootFolderId)

            // Create subfolders
            val timetablesFolder = getOrCreateFolder("Timetables", userFolder.id)
            val studentsFolder = getOrCreateFolder("Students", userFolder.id)
            val documentsFolder = getOrCreateFolder("Documents", userFolder.id)

            UserFolderStructure(
                rootFolder = userFolder,
                timetablesFolder = timetablesFolder,
                studentsFolder = studentsFolder,
                documentsFolder = documentsFolder
            )
        }

    /**
     * List files in a folder
     */
    suspend fun listFiles(folderId: String, mimeType: String? = null): List<File> =
        withContext(Dispatchers.IO) {
            var query = "'$folderId' in parents and trashed = false"
            if (mimeType != null) {
                query += " and mimeType = '$mimeType'"
            }

            val result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name, mimeType, size, createdTime, modifiedTime, webViewLink)")
                .setOrderBy("modifiedTime desc")
                .execute()

            result.files ?: emptyList()
        }

    /**
     * Upload a file to a folder
     */
    suspend fun uploadFile(
        fileName: String,
        mimeType: String,
        content: ByteArray,
        parentFolderId: String
    ): File = withContext(Dispatchers.IO) {
        val fileMetadata = File().apply {
            name = fileName
            parents = listOf(parentFolderId)
        }

        // Create temp file for upload
        val tempFile = java.io.File.createTempFile("upload_", null, context.cacheDir)
        tempFile.writeBytes(content)

        try {
            val mediaContent = FileContent(mimeType, tempFile)
            driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name, mimeType, size, webViewLink")
                .execute()
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Upload a file from InputStream
     */
    suspend fun uploadFile(
        fileName: String,
        mimeType: String,
        inputStream: InputStream,
        parentFolderId: String
    ): File = withContext(Dispatchers.IO) {
        val content = inputStream.readBytes()
        uploadFile(fileName, mimeType, content, parentFolderId)
    }

    /**
     * Download a file's content
     */
    suspend fun downloadFile(fileId: String): ByteArray = withContext(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        driveService.files().get(fileId)
            .executeMediaAndDownloadTo(outputStream)
        outputStream.toByteArray()
    }

    /**
     * Delete a file or folder
     */
    suspend fun deleteFile(fileId: String): Unit = withContext(Dispatchers.IO) {
        driveService.files().delete(fileId).execute()
    }

    /**
     * Update file content
     */
    suspend fun updateFile(
        fileId: String,
        mimeType: String,
        content: ByteArray
    ): File = withContext(Dispatchers.IO) {
        val tempFile = java.io.File.createTempFile("update_", null, context.cacheDir)
        tempFile.writeBytes(content)

        try {
            val mediaContent = FileContent(mimeType, tempFile)
            driveService.files().update(fileId, null, mediaContent)
                .setFields("id, name, mimeType, size, modifiedTime, webViewLink")
                .execute()
        } finally {
            tempFile.delete()
        }
    }
}

/**
 * Data class representing a user's folder structure in Google Drive
 */
data class UserFolderStructure(
    val rootFolder: File,
    val timetablesFolder: File,
    val studentsFolder: File,
    val documentsFolder: File
)
