package com.github.diogocerqueiralima.schema.utils

import java.util.Random
import java.util.UUID

/**
 * Utility object for generating and handling UUIDv7.
 */
object UUIDv7 {

    private val RANDOM = Random()

    /**
     * Generates a UUIDv7 from the given timestamp.
     *
     * @param timestamp the timestamp in milliseconds
     * @return a UUIDv7
     */
    fun from(timestamp: Long): UUID {

        var mostSigBits = 0L
        var leastSigBits = 0L

        mostSigBits = mostSigBits or ((timestamp and 0xFFFFFFFFFFFFL) shl 16) // Lower 48 bits for timestamp
        mostSigBits = mostSigBits or (0x7L shl 12) // Version 7
        mostSigBits = mostSigBits or (RANDOM.nextInt(0x1000).toLong() and 0xFFF) // random 12 bits

        leastSigBits = leastSigBits or (0x2L shl 62) // variant bits
        leastSigBits = leastSigBits or (RANDOM.nextLong() and 0x3FFFFFFFFFFFFFFFL) // random 62 bits

        return UUID(mostSigBits, leastSigBits)
    }

    /**
     * Creates a new UUIDv7 with the current timestamp.
     *
     * @return a UUIDv7
     */
    fun create(): UUID = from(System.currentTimeMillis())

    /**
     * Extracts the timestamp from a UUIDv7.
     *
     * @param uuid the UUIDv7
     * @return the timestamp in milliseconds
     */
    fun extractTimestamp(uuid: UUID): Long = (uuid.mostSignificantBits shr 16) and 0xFFFFFFFFFFFFL

}
