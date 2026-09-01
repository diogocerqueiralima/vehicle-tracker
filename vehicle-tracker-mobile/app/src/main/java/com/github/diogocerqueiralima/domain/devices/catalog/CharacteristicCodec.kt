package com.github.diogocerqueiralima.domain.devices.catalog

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Decodes a characteristic's raw value into a human-readable string, matching the wire
 * format used by the device firmware (see vehicle-tracker-embedded/main/ble/services):
 * fixed-width little-endian integers, single-byte booleans, and raw UTF-8 strings.
 */
object CharacteristicCodec {

    fun decode(value: ByteArray, format: CharacteristicFormat): String = when (format) {

        CharacteristicFormat.STRING -> String(value, Charsets.UTF_8)

        CharacteristicFormat.BOOLEAN -> if (value.isNotEmpty() && value[0] != 0.toByte()) "true" else "false"

        CharacteristicFormat.UINT8 -> value.firstOrNull()?.let { (it.toInt() and 0xFF).toString() } ?: "-"

        CharacteristicFormat.UINT16 -> value.toLittleEndianLong(2).toString()

        CharacteristicFormat.UINT32 -> value.toLittleEndianLong(4).toString()

    }

    /**
     * Encodes a human-readable string into a characteristic's raw wire value, inverse of [decode].
     *
     * @throws NumberFormatException if [value] isn't a valid number for a numeric [format].
     */
    fun encode(value: String, format: CharacteristicFormat): ByteArray = when (format) {

        CharacteristicFormat.STRING -> value.toByteArray(Charsets.UTF_8)

        CharacteristicFormat.BOOLEAN -> byteArrayOf(if (value.trim().toBooleanStrict()) 1 else 0)

        CharacteristicFormat.UINT8 -> byteArrayOf((value.trim().toInt() and 0xFF).toByte())

        CharacteristicFormat.UINT16 -> value.trim().toLong().toLittleEndianByteArray(2)

        CharacteristicFormat.UINT32 -> value.trim().toLong().toLittleEndianByteArray(4)

    }

    private fun ByteArray.toLittleEndianLong(bytes: Int): Long {

        if (size != bytes) return 0L

        val padded = ByteArray(8)
        copyInto(padded, destinationOffset = 0, startIndex = 0, endIndex = size)

        return ByteBuffer.wrap(padded).order(ByteOrder.LITTLE_ENDIAN).long
    }

    private fun Long.toLittleEndianByteArray(bytes: Int): ByteArray =
        ByteBuffer.allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(this)
            .array()
            .copyOf(bytes)

}