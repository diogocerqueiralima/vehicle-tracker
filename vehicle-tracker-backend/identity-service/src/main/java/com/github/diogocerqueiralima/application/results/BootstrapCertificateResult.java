package com.github.diogocerqueiralima.application.results;

import java.math.BigInteger;
import java.time.Instant;

public record BootstrapCertificateResult(
        BigInteger serialNumber, String subject, Instant issuedAt, Instant expiresAt, boolean revoked, boolean used
) {}
