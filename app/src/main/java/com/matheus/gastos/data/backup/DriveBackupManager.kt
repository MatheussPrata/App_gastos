package com.matheus.gastos.data.backup

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.FileOutputStream
import java.io.InputStream

class DriveBackupManager(private val context: Context) {

    private val SCOPES = listOf(DriveScopes.DRIVE_FILE)
    private var driveService: Drive? = null

    init {
        // Initialize with default account - will be set when user chooses
    }

    fun setAccount(accountName: String) {
        val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES)
        credential.selectedAccountName = accountName
        driveService = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Gastos App")
            .build()
    }

    suspend fun backupDatabase(databasePath: String): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        driveService?.let { service ->
            val fileMetadata = File().setName("gastos-backup-${System.currentTimeMillis()}.db")
            val mediaContent = com.google.api.client.http.FileContent("application/octet-stream", java.io.File(databasePath))
            val file = service.files().create(fileMetadata, mediaContent).setFields("id").execute()
            file.id
        }
    }

    suspend fun restoreDatabase(databasePath: String, fileId: String): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        driveService?.let { service ->
            val outputStream = FileOutputStream(databasePath)
            try {
                service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                true
            } finally {
                outputStream.close()
            }
        } ?: false
    }

    suspend fun listBackups(): List<BackupFile> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        driveService?.let { service ->
            val result = service.files()
                .list()
                .setQ("name contains 'gastos-backup-' and mimeType='application/octet-stream'")
                .setOrderBy("createdTime desc")
                .setFields("files(id,name,createdTime,size)")
                .execute()
            result.files?.map { BackupFile(it.id, it.name, it.createdTime.toString(), it.size?.toLong() ?: 0) } ?: emptyList()
        } ?: emptyList()
    }

    suspend fun deleteBackup(fileId: String): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        driveService?.let { service ->
            service.files().delete(fileId).execute()
            true
        } ?: false
    }

    data class BackupFile(
        val id: String,
        val name: String,
        val createdTime: String,
        val size: Long
    )
}
