package com.github.diogocerqueiralima.infrastructure.common.storage

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException
import com.github.diogocerqueiralima.domain.common.storage.FileStorage

import java.io.InputStream
import java.io.OutputStream

/**
 * [FileStorage] implementation backed by [ContentResolver], persisting downloads to the user's
 * Downloads collection via MediaStore and reading files picked through the Storage Access
 * Framework.
 */
class MediaStoreFileStorage(
    private val contentResolver: ContentResolver
) : FileStorage {

    override suspend fun saveToDownloads(displayName: String, mimeType: String, write: suspend (OutputStream) -> Unit): String {

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw InternalErrorException("Could not create a Downloads entry")

        try {

            val sink = contentResolver.openOutputStream(uri)
                ?: throw InternalErrorException("Could not open the Downloads entry for writing")

            sink.use { write(it) }

            return savedName(uri) ?: displayName
        } catch (exception: Exception) {
            contentResolver.delete(uri, null, null)
            throw exception
        }
    }

    override suspend fun size(uri: Uri): Long? =
        contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else null
        }

    override suspend fun openInputStream(uri: Uri): InputStream? =
        contentResolver.openInputStream(uri)

    /**
     * The display name [uri] was actually saved under, which MediaStore may have changed from
     * the one requested on a name collision, or `null` if it can't be determined.
     */
    private fun savedName(uri: Uri): String? =
        contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

}
