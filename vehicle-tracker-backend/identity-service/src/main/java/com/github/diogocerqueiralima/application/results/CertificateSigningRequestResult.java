package com.github.diogocerqueiralima.application.results;

import java.time.Instant;

public record CertificateSigningRequestResult(
        String serialNumber,
        String subject,
        Instant issuedAt,
        Instant expiresAt,
        boolean revoked,
        byte[] data
) {}
