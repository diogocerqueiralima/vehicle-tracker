package com.github.diogocerqueiralima.utils;

import java.util.Random;
import java.util.UUID;

/**
 * Utility class for generating and handling UUIDv7.
 */
public class UUIDv7 {

    private final static Random RANDOM = new Random();

    /**
     *
     * Generates a UUIDv7 from the given timestamp.
     *
     * @param timestamp the timestamp in milliseconds
     * @return a UUIDv7
     */
    public static UUID from(long timestamp) {

        long mostSigBits = 0L;
        long leastSigBits = 0L;

        mostSigBits |= (timestamp & 0xFFFFFFFFFFFFL) << 16; // Lower 48 bits for timestamp
        mostSigBits |= 0x7 << 12; // Version 7
        mostSigBits |= (RANDOM.nextInt(0x1000) & 0xFFF); // random 12 bits

        leastSigBits |= 0x2L << 62; // variant bits
        leastSigBits |= (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL); // random 62 bits

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     *
     * Creates a new UUIDv7 with the current timestamp.
     *
     * @return a UUIDv7
     */
    public static UUID create() {
        long timestamp = System.currentTimeMillis();
        return from(timestamp);
    }

    /**
     *
     * Extracts the timestamp from a UUIDv7.
     *
     * @param uuid the UUIDv7
     * @return the timestamp in milliseconds
     */
    public static long extractTimestamp(UUID uuid) {
        long mostSigBits = uuid.getMostSignificantBits();
        return (mostSigBits >> 16) & 0xFFFFFFFFFFFFL;
    }

}
