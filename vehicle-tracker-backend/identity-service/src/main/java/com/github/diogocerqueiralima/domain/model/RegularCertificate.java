package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;
import com.github.diogocerqueiralima.domain.model.options.RevokeCertificateOptions;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Concrete implementation of a regular certificate, which can be revoked.
 *
 * @see AbstractCertificate
 * @see Certificate
 * @see RevokeCertificateOptions
 */
public class RegularCertificate extends AbstractCertificate {

    private final RevokeCertificateOptions options;

    protected RegularCertificate(
            BigInteger serialNumber, CertificateSubject subject, Instant issuedAt, Instant expiresAt,
            RevokeCertificateOptions options
    ) {
        super(serialNumber, subject, issuedAt, expiresAt);
        this.options = options;
    }

    public RegularCertificate(
            BigInteger serialNumber, CertificateSubject subject, Instant issuedAt, Instant expiresAt,
            boolean revoked
    ) {
        this(serialNumber, subject, issuedAt, expiresAt, new RevokeCertificateOptions(revoked));
    }

    @Override
    public Certificate revoke() {

        if (options.isRevoked()) {
            throw new CertificateRevokedException(getSerialNumber());
        }

        return new RegularCertificate(getSerialNumber(), getSubject(), getIssuedAt(), getExpiresAt(), true);
    }

    @Override
    public boolean isRevoked() {
        return options.isRevoked();
    }

}
