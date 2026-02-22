package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.domain.exceptions.BootstrapCertificateUsedException;
import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Represents a bootstrap certificate with an additional attribute to track its usage status.
 * This certificate is only intended for one-time use during the bootstrap process.
 */
public class BootstrapCertificate extends Certificate {

    private final boolean used;

    /**
     *
     * Creates a new instance of BootstrapCertificate with the specified attributes, including the used status.
     *
     * @param serialNumber the unique serial number of the certificate
     * @param subject the subject information associated with the certificate
     * @param issuedAt the timestamp when the certificate was issued
     * @param expiresAt the timestamp when the certificate expires
     * @param revoked the revocation status of the certificate
     * @param used the usage status of the certificate, indicating whether it has been used during the bootstrap process
     */
    public BootstrapCertificate(
            BigInteger serialNumber, CertificateSubject subject, Instant issuedAt, Instant expiresAt,
            boolean revoked, boolean used
    ) {
        super(serialNumber, subject, issuedAt, expiresAt, revoked);
        this.used = used;
    }

    /**
     *
     * Marks the bootstrap certificate as used by returning a new instance with the used status set to true.
     *
     * @return a new BootstrapCertificate instance with used status set to true
     * @throws BootstrapCertificateUsedException if the certificate is already marked as used
     * @throws CertificateRevokedException if the certificate is revoked
     */
    public BootstrapCertificate markAsUsed() {

        if (this.isRevoked()) {
            throw new CertificateRevokedException(this.getSerialNumber());
        }

        if (this.used) {
            throw new BootstrapCertificateUsedException(this);
        }

        return new BootstrapCertificate(
                this.getSerialNumber(), this.getSubject(), this.getIssuedAt(), this.getExpiresAt(),
                this.isRevoked(), true
        );
    }

    public boolean isUsed() {
        return used;
    }

}
