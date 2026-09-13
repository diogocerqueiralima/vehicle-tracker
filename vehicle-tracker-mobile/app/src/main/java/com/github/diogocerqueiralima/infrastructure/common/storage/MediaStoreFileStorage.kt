package com.github.diogocerqueiralima.infrastructure.common.storage

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException
import com.github.diogocerqueiralima.domain.common.storage.FileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    @SuppressLint("Recycle")
    override suspend fun saveTo(
        displayName: String,
        mimeType: String,
        path: String,
        write: suspend (OutputStream) -> Unit
    ): String {

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, path)
        }

        val uri = withContext(Dispatchers.IO) { contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) }
            ?: throw InternalErrorException("Could not create a Downloads entry")

        try {

            val sink = withContext(Dispatchers.IO) { contentResolver.openOutputStream(uri) }
                ?: throw InternalErrorException("Could not open the Downloads entry for writing")

            sink.use { write(it) }

            return savedName(uri) ?: displayName
        } catch (exception: Exception) {
            withContext(Dispatchers.IO) { contentResolver.delete(uri, null, null) }
            throw exception
        }
    }

    @SuppressLint("Recycle")
    override suspend fun readFrom(uri: Uri, read: suspend (source: InputStream, length: Long) -> Unit) {

        val length = size(uri) ?: throw InternalErrorException("Could not determine the file's size")
        val source = withContext(Dispatchers.IO) { contentResolver.openInputStream(uri) }
            ?: throw InternalErrorException("Could not open the picked file for reading")

        source.use { read(it, length) }
    }

    /**
     * The size, in bytes, of the file at [uri], or `null` if it can't be determined.
     */
    private suspend fun size(uri: Uri): Long? = withContext(Dispatchers.IO) {
        contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
    }

    /**
     * The display name [uri] was actually saved under, which MediaStore may have changed from
     * the one requested on a name collision, or `null` if it can't be determined.
     */
    private suspend fun savedName(uri: Uri): String? = withContext(Dispatchers.IO) {
        contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

}
