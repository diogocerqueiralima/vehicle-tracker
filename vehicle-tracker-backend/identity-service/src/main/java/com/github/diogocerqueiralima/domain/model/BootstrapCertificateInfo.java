package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.domain.exceptions.BootstrapCertificateUsedException;
import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;

import java.time.Instant;

/**
 * Represents a bootstrap certificate with an additional attribute to track its usage status.
 * This certificate is only intended for one-time use during the bootstrap process.
 */
public class BootstrapCertificateInfo extends CertificateInfo {

    private final boolean used;

    /**
     *
     * Instantiates a new BootstrapCertificate.
     *
     * @param serialNumber the unique identifier for the certificate
     * @param subject the entity to which the certificate is issued
     * @param issuedAt the timestamp when the certificate was issued
     * @param expiresAt the timestamp when the certificate will expire
     * @param revoked the status indicating whether the bootstrap certificate has been revoked
     * @param used the status indicating whether the bootstrap certificate has been used
     */
    public BootstrapCertificateInfo(
            String serialNumber, String subject, Instant issuedAt, Instant expiresAt, boolean revoked, boolean used
    ) {
        super(serialNumber, subject, issuedAt, expiresAt, revoked);
        this.used = used;
    }

    /**
     *
     * Instantiates a new BootstrapCertificate with automatic issuedAt, expiresAt calculation, not revoked, and not used.
     *
     * @param serialNumber the unique identifier for the certificate
     * @param subject the entity to which the certificate is issued
     * @param expiresAfter the duration in nanoseconds after which the certificate will expire from the issuedAt time
     */
    public BootstrapCertificateInfo(String serialNumber, String subject, long expiresAfter) {
        this(serialNumber, subject, Instant.now(), Instant.now().plusNanos(expiresAfter), false, false);
    }

    /**
     *
     * Marks the bootstrap certificate as used by returning a new instance with the used status set to true.
     *
     * @return a new BootstrapCertificate instance with used status set to true
     * @throws BootstrapCertificateUsedException if the certificate is already marked as used
     * @throws CertificateRevokedException if the certificate is revoked
     */
    public BootstrapCertificateInfo markAsUsed() {

        if (this.isRevoked()) {
            throw new CertificateRevokedException(this);
        }

        if (this.used) {
            throw new BootstrapCertificateUsedException(this);
        }

        return new BootstrapCertificateInfo(
                this.getSerialNumber(),
                this.getSubject(),
                this.getIssuedAt(),
                this.getExpiresAt(),
                this.isRevoked(),
                true
        );
    }

    public boolean isUsed() {
        return used;
    }

}
