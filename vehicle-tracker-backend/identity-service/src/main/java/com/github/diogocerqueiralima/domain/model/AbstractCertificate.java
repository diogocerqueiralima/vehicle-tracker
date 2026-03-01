package com.github.diogocerqueiralima.domain.model;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Abstract base class for certificates, providing common properties and methods.
 *
 * @see Certificate
 */
public abstract class AbstractCertificate implements Certificate {

    private final BigInteger serialNumber;
    private final CertificateSubject subject;
    private final Instant issuedAt;
    private final Instant expiresAt;

    protected AbstractCertificate(BigInteger serialNumber, CertificateSubject subject, Instant issuedAt, Instant expiresAt) {
        this.serialNumber = serialNumber;
        this.subject = subject;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    @Override
    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    @Override
    public CertificateSubject getSubject() {
        return subject;
    }

    @Override
    public Instant getIssuedAt() {
        return issuedAt;
    }

    @Override
    public Instant getExpiresAt() {
        return expiresAt;
    }

}
