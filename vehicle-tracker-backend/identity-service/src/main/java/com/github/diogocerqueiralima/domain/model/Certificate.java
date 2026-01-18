package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;

import java.time.Instant;

public class Certificate {

    private final String serialNumber;
    private final String subject;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final boolean revoked;

    /**
     *
     * Instantiates a new Certificate.
     * This represents a digital certificate with its essential attributes.
     *
     * @param serialNumber the unique identifier for the certificate
     * @param subject the entity to which the certificate is issued
     * @param issuedAt the timestamp when the certificate was issued
     * @param expiresAt the timestamp when the certificate will expire
     * @param revoked the status indicating whether the certificate has been revoked
     */
    public Certificate(String serialNumber, String subject, Instant issuedAt, Instant expiresAt, boolean revoked) {
        this.serialNumber = serialNumber;
        this.subject = subject;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    /**
     *
     * Instantiates a new Certificate with automatic issuedAt and expiresAt calculation.
     *
     * @param serialNumber the unique identifier for the certificate
     * @param subject the entity to which the certificate is issued
     * @param expiresAfter the duration in nanoseconds after which the certificate will expire from the issuedAt time
     */
    public Certificate(String serialNumber, String subject, Long expiresAfter) {
        this(serialNumber, subject, Instant.now(), Instant.now().plusNanos(expiresAfter), false);
    }

    /**
     *
     * Revokes the certificate by returning a new instance with the revoked status set to true.
     *
     * @return a new Certificate instance with revoked status set to true
     * @throws CertificateRevokedException if the certificate is already revoked
     */
    public Certificate revoke() {

        if (this.revoked) {
            throw new CertificateRevokedException(this);
        }

        return new Certificate(this.serialNumber, this.subject, this.issuedAt, this.expiresAt, true);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     *
     * Checks if the certificate is revoked.
     *
     * @return true if the certificate is revoked, false otherwise
     */
    public boolean isRevoked() {
        return this.revoked;
    }

}
