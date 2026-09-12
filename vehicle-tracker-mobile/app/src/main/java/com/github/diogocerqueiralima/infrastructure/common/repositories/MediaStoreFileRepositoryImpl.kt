package com.github.diogocerqueiralima.infrastructure.common.repositories

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException
import com.github.diogocerqueiralima.domain.common.repositories.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "MEDIA_STORE_FILE_REPOSITORY"

/**
 * Implementation of [FileRepository] writing to the shared downloads collection through
 * [MediaStore], which needs no storage permission and puts the file where the user's file manager
 * and other apps can reach it.
 */
class MediaStoreFileRepositoryImpl(private val contentResolver: ContentResolver) : FileRepository {

    override suspend fun download(name: String, mimeType: String, content: ByteArray): String = withContext(Dispatchers.IO) {

        Log.d(TAG, "Saving $name to downloads, ${content.size} bytes")

        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        // 1. Create the entry as pending, so nothing else sees a half-written file.
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(collection, values)
            ?: throw InternalErrorException("Failed to create $name in the downloads collection")

        // 2. Fill the entry in and publish it, removing it if any of that fails: an entry left
        // pending is invisible to the user and would never be cleaned up.
        val savedName = try {

            contentResolver.openOutputStream(uri)?.use { output -> output.write(content) }
                ?: throw InternalErrorException("Failed to open $name for writing")

            // 2.1 Clearing the pending flag is what makes the file visible.
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            contentResolver.update(uri, values, null, null)

            // 2.2 Report the name it ended up with, which MediaStore changes when the name is taken.
            contentResolver
                .query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                ?: name

        } catch (exception: Exception) {
            Log.e(TAG, "Failed to save $name, removing the entry", exception)
            contentResolver.delete(uri, null, null)
            throw exception
        }

        Log.d(TAG, "Saved $savedName to downloads")

        savedName
    }

}
