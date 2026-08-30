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

    private fun ByteArray.toLittleEndianLong(bytes: Int): Long {

        if (size != bytes) return 0L

        val padded = ByteArray(8)
        copyInto(padded, destinationOffset = 0, startIndex = 0, endIndex = size)

        return ByteBuffer.wrap(padded).order(ByteOrder.LITTLE_ENDIAN).long
    }

}