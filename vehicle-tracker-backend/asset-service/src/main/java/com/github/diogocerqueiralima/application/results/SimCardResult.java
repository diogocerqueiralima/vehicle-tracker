package com.github.diogocerqueiralima.application.results;

/**
 * Result returned by SIM card application use cases.
 */
public record SimCardResult(
        String iccid,
        String msisdn,
        String imsi
) {}

