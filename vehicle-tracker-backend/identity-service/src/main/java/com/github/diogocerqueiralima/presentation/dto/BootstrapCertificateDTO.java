package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigInteger;
import java.time.Instant;

@Schema(
        description = """
                Represents the details of a bootstrap certificate,
                including its serial number, subject, issuance and expiration dates, and status flags.
                """
)
public record BootstrapCertificateDTO(

        @Schema(description = "The unique serial number of the certificate.")
        @JsonProperty("serial_number")
        BigInteger serialNumber,

        @Schema(description = "The subject or entity to which the certificate is issued.")
        String subject,

        @Schema(description = "The timestamp when the certificate was issued.")
        @JsonProperty("issued_at")
        Instant issuedAt,

        @Schema(description = "The timestamp when the certificate will expire.")
        @JsonProperty("expires_at")
        Instant expiresAt,

        @Schema(description = "Indicates whether the certificate has been revoked.")
        boolean revoked,

        @Schema(description = "Indicates whether the certificate has been used for generate a new certificate.")
        boolean used

) {}
