package com.github.diogocerqueiralima.asset.service.presentation.grpc.mappers

import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Converts this instant to a protobuf [Timestamp], truncated to millisecond precision.
 */
fun Instant.toProtoTimestamp(): Timestamp {

    val instant = truncatedTo(ChronoUnit.MILLIS)

    return timestamp {
        seconds = instant.epochSecond
        nanos = instant.nano
    }
}
