package com.github.diogocerqueiralima.application.results;

/**
 * Result returned by SIM card application use cases.
 */
public record SimCardResult(
        java.util.UUID id,
        java.time.Instant createdAt,
        java.time.Instant updatedAt,
        String iccid,
        String msisdn,
        String imsi
) {}

