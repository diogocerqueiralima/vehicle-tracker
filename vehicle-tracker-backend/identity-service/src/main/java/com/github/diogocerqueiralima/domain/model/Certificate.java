package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Represents a digital certificate with associated information and revocation status.
 */
public class Certificate {

    private final BigInteger serialNumber;
    private final CertificateSubject subject;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final boolean revoked;

    /**
     *
     * Creates a new instance of Certificate with the specified attributes, including the revoked status.
     *
     * @param serialNumber the unique serial number of the certificate
     * @param subject the subject information associated with the certificate
     * @param issuedAt the timestamp when the certificate was issued
     * @param expiresAt the timestamp when the certificate expires
     * @param revoked the revocation status of the certificate, indicating whether it has been revoked
     */
    public Certificate(
            BigInteger serialNumber, CertificateSubject subject, Instant issuedAt, Instant expiresAt, boolean revoked
    ) {
        this.serialNumber = serialNumber;
        this.subject = subject;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    /**
     *
     * Creates a new instance of Certificate with the specified attributes, defaulting the revoked status to false.
     *
     * @param serialNumber the unique serial number of the certificate
     * @param subject the subject information associated with the certificate
     * @param issuedAt the timestamp when the certificate was issued
     * @param expiresAt the timestamp when the certificate expires
     */
    public Certificate(BigInteger serialNumber, CertificateSubject subject, Instant issuedAt, Instant expiresAt) {
        this(serialNumber, subject, issuedAt, expiresAt, false);
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
            throw new CertificateRevokedException(this.serialNumber);
        }

        return new Certificate(this.serialNumber, this.subject, this.issuedAt, this.expiresAt, true);
    }

    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    public CertificateSubject getSubject() {
        return subject;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

}
