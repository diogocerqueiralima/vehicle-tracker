package com.github.diogocerqueiralima.utils

import kotlinx.serialization.json.Json
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

const val TRANSFORMATION = "AES/GCM/NoPadding"

/**
 *
 * Encrypts an object of type T using the provided SecretKey.
 * The object is first serialized to a JSON string before encryption.
 *
 * @param T The type of the object to be encrypted.
 * @param obj The object to be encrypted.
 * @param key The SecretKey used for encryption.
 * @return A ByteArray containing the encrypted data.
 */
inline fun <reified T> encrypt(obj: T, key: SecretKey): ByteArray {

    val serializedObject = Json.encodeToString(obj)
    val cipher = Cipher.getInstance(TRANSFORMATION)

    cipher.init(Cipher.ENCRYPT_MODE, key)

    val iv = cipher.iv

    return iv + cipher.doFinal(serializedObject.toByteArray(Charsets.UTF_8))
}

/**
 *
 * Decrypts a ByteArray into an object of type T using the provided SecretKey.
 * The decrypted data is deserialized from a JSON string back into the object.
 *
 * @param T The type of the object to be decrypted.
 * @param data The ByteArray containing the encrypted data.
 * @param key The SecretKey used for decryption.
 * @return The decrypted object of type T.
 */
inline fun <reified T> decrypt(data: ByteArray, key: SecretKey): T {

    val iv = data.sliceArray(0 until 12)
    val encryptedBytes = data.sliceArray(12 until data.size)
    val cipher = Cipher.getInstance(TRANSFORMATION)

    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))

    val decryptedBytes = cipher.doFinal(encryptedBytes)
    val decryptedString = decryptedBytes.toString(Charsets.UTF_8)

    return Json.decodeFromString(decryptedString)
}