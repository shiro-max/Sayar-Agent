package com.sayar.assistant.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val size: Long? = null,
    val createdTime: String? = null,
    val modifiedTime: String? = null,
    val webViewLink: String? = null
)

@Serializable
data class UserDriveFolders(
    val userEmail: String,
    val rootFolderId: String,
    val timetablesFolderId: String,
    val studentsFolderId: String,
    val documentsFolderId: String
) {
    fun getFolderId(folderType: com.sayar.assistant.domain.repository.FolderType): String {
        return when (folderType) {
            com.sayar.assistant.domain.repository.FolderType.TIMETABLES -> timetablesFolderId
            com.sayar.assistant.domain.repository.FolderType.STUDENTS -> studentsFolderId
            com.sayar.assistant.domain.repository.FolderType.DOCUMENTS -> documentsFolderId
        }
    }
}
