package com.github.diogocerqueiralima.infrastructure.repositories

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.github.diogocerqueiralima.domain.repositories.KeyRepository
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Implementation of [KeyRepository] that manages SecretKeys in the Android Keystore.
 */
class KeyRepositoryImpl(val keyStore: KeyStore) : KeyRepository {

    override fun getOrCreate(alias: String): SecretKey {
        val existingKey = keyStore.getKey(alias, null) as? SecretKey
        return existingKey ?: createKey(alias)
    }

    /**
     *
     * Creates a new AES SecretKey in the Android Keystore with the given alias.
     * It is configured to use GCM block mode and no padding.
     *
     * @param alias The alias for the key to be created.
     * @return The newly created SecretKey.
     */
    private fun createKey(alias: String): SecretKey {

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        generator.init(parameterSpec)
        return generator.generateKey()
    }

}