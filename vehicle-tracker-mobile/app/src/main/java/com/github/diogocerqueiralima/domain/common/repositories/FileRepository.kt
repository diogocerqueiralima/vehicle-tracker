package com.github.diogocerqueiralima.domain.common.repositories

/**
 * Interface for handing a file to the user, outside the app's own storage.
 */
interface FileRepository {

    /**
     * Saves [content] to the user's downloads under [name], as a file of type [mimeType].
     *
     * @param name The name to save the file under, extension included.
     * @param mimeType The media type of the file being saved.
     * @param content The bytes to write, saved unchanged.
     * @return The name the file was actually saved under, which differs from [name] when one of
     * that name is already there.
     * @throws com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException if the
     * file cannot be created or written.
     */
    suspend fun download(name: String, mimeType: String, content: ByteArray): String

}
