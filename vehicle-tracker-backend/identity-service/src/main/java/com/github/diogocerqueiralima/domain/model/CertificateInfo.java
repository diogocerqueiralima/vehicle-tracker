package com.github.diogocerqueiralima.domain.model;

import java.time.Instant;

/**
 * Represents a digital certificate with essential attributes such as serial number,
 * subject, issuance and expiration timestamps, and revocation status.
 */
public class CertificateInfo {

    private final String serialNumber;
    private final String subject;
    private final Instant issuedAt;
    private final Instant expiresAt;

    /**
     *
     * Instantiates a new Certificate.
     *
     * @param serialNumber the unique identifier for the certificate
     * @param subject the entity to which the certificate is issued
     * @param issuedAt the timestamp when the certificate was issued
     * @param expiresAt the timestamp when the certificate will expire
     */
    public CertificateInfo(String serialNumber, String subject, Instant issuedAt, Instant expiresAt) {
        this.serialNumber = serialNumber;
        this.subject = subject;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    /**
     *
     * Instantiates a new Certificate with automatic issuedAt, expiresAt calculation and not revoked.
     *
     * @param serialNumber the unique identifier for the certificate
     * @param subject the entity to which the certificate is issued
     * @param expiresAfter the duration in nanoseconds after which the certificate will expire from the issuedAt time
     */
    public CertificateInfo(String serialNumber, String subject, long expiresAfter) {
        this(serialNumber, subject, Instant.now(), Instant.now().plusNanos(expiresAfter));
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getSubject() {
        return subject;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }


}
