package com.github.diogocerqueiralima.presentation.dto;

import java.time.Instant;

public record BootstrapCertificateDTO(
        String serialNumber, String subject, Instant issuedAt, Instant expiresAt, boolean revoked, boolean used
) {}
