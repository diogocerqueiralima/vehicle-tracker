package com.github.diogocerqueiralima.application.results;

import java.time.Instant;

public record BootstrapCertificateResult(
        String serialNumber, String subject, Instant issuedAt, Instant expiresAt, boolean revoked, boolean used
) {}
