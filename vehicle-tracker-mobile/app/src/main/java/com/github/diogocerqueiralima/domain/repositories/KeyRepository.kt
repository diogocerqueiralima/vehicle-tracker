package com.github.diogocerqueiralima.domain.repositories

import javax.crypto.SecretKey

const val USER_SESSION_KEY = "USER_SESSION_KEY"

/**
 * Repository interface for managing SecretKeys.
 */
interface KeyRepository {

    /**
     *
     * Retrieves an existing SecretKey or creates a new one with the given alias.
     *
     * @param alias The alias for the SecretKey.
     * @return The existing or newly created SecretKey.
     */
    fun getOrCreate(alias: String): SecretKey

}
