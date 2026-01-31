package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.domain.exceptions.BootstrapCertificateUsedException;
import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;

/**
 * Represents a bootstrap certificate with an additional attribute to track its usage status.
 * This certificate is only intended for one-time use during the bootstrap process.
 */
public class BootstrapCertificate extends Certificate {

    private final boolean used;

    /**
     *
     * Instantiates a new BootstrapCertificate.
     *
     * @param info the certificate information
     * @param revoked the status indicating whether the bootstrap certificate has been revoked
     * @param used the status indicating whether the bootstrap certificate has been used
     */
    public BootstrapCertificate(CertificateInfo info, boolean revoked, boolean used) {
        super(info, revoked);
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
            throw new CertificateRevokedException(this.getInfo());
        }

        if (this.used) {
            throw new BootstrapCertificateUsedException(this);
        }

        return new BootstrapCertificate(this.getInfo(), this.isRevoked(), true);
    }

    public boolean isUsed() {
        return used;
    }

}
