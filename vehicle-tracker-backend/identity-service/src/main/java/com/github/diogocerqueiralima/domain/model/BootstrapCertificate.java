package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.domain.exceptions.BootstrapCertificateUsedException;
import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;
import com.github.diogocerqueiralima.domain.model.options.OneTimeCertificateOptions;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Concrete implementation of a bootstrap certificate, which can be revoked and marked as used.
 *
 * @see AbstractCertificate
 * @see Certificate
 * @see OneTimeCertificateOptions
 */
public class BootstrapCertificate extends AbstractCertificate {

    private final OneTimeCertificateOptions options;

    protected BootstrapCertificate(
            BigInteger serialNumber, CertificateSubject subject, Instant issuedAt, Instant expiresAt,
            OneTimeCertificateOptions options
    ) {
        super(serialNumber, subject, issuedAt, expiresAt);
        this.options = options;
    }

    /**
     *
     * Creates a new BootstrapCertificate with the given parameters and options.
     *
     * @param serialNumber the unique serial number of the certificate
     * @param subject the subject information of the certificate
     * @param issuedAt the timestamp when the certificate was issued
     * @param expiresAt the timestamp when the certificate expires
     * @param revoked indicates whether the certificate is revoked
     * @param used indicates whether the certificate has been marked as used
     */
    public BootstrapCertificate(
            BigInteger serialNumber, CertificateSubject subject, Instant issuedAt, Instant expiresAt,
            boolean revoked, boolean used
    ) {
        this(serialNumber, subject, issuedAt, expiresAt, new OneTimeCertificateOptions(revoked, used));
    }

    @Override
    public BootstrapCertificate revoke() {

        if (options.isRevoked()) {
            throw new CertificateRevokedException(getSerialNumber());
        }

        return new BootstrapCertificate(getSerialNumber(), getSubject(), getIssuedAt(), getExpiresAt(), true, options.isUsed());
    }

    @Override
    public boolean isRevoked() {
        return options.isRevoked();
    }

    /**
     * Marks the certificate as used.
     *
     * @return a new instance of BootstrapCertificate with the used status set to true
     * @throws BootstrapCertificateUsedException if the certificate has already been marked as used
     */
    public BootstrapCertificate markAsUsed() {

        if (options.isUsed()) {
            throw new BootstrapCertificateUsedException(this);
        }

        return new BootstrapCertificate(getSerialNumber(), getSubject(), getIssuedAt(), getExpiresAt(), options.isRevoked(), true);
    }

    /**
     * Returns whether the certificate has been marked as used.
     * A used certificate is considered invalid for future use and should not be accepted for authentication or authorization purposes.
     *
     * @return true if the certificate has been marked as used, false otherwise
     */
    public boolean isUsed() {
        return options.isUsed();
    }

}
