package com.github.diogocerqueiralima.domain.common.storage

import android.net.Uri
import android.os.Environment
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
    suspend fun saveTo(
        displayName: String,
        mimeType: String,
        path: String = Environment.DIRECTORY_DOWNLOADS,
        write: suspend (OutputStream) -> Unit
    ): String

    /**
     * Opens the file at [uri] for reading, passing its size and an input stream to [read]. The
     * stream is closed once [read] returns or throws.
     *
     * @throws InternalErrorException if [uri]'s size can't be determined or it can't be opened
     * for reading.
     */
    suspend fun readFrom(uri: Uri, read: suspend (source: InputStream, length: Long) -> Unit)

}
