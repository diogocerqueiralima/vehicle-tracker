package com.github.diogocerqueiralima.domain.common.storage

import android.net.Uri
import com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException

import java.io.InputStream
import java.io.OutputStream

/**
 * Interface for persisting and reading files, decoupling that persistence from the underlying
 * storage APIs (MediaStore, the Storage Access Framework).
 */
interface FileStorage {

    /**
     * Creates a new entry named [displayName] with [mimeType] in the user's Downloads
     * collection, streams bytes into it via [write], and returns the display name it was
     * actually saved under, which may differ from [displayName] on a name collision. The entry
     * is deleted if [write] throws, so a failed download never leaves behind a partial file.
     *
     * @throws InternalErrorException if the entry can't be created or opened for writing.
     */
    suspend fun saveToDownloads(displayName: String, mimeType: String, write: suspend (OutputStream) -> Unit): String

    /**
     * The size, in bytes, of the file at [uri], or `null` if it can't be determined.
     */
    suspend fun size(uri: Uri): Long?

    /**
     * Opens the file at [uri] for reading, or `null` if it can't be opened.
     */
    suspend fun openInputStream(uri: Uri): InputStream?

}
