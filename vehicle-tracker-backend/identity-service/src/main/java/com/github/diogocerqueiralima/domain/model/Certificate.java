package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;

/**
 * Represents a digital certificate with associated information and revocation status.
 */
public class Certificate {

    private final CertificateInfo info;
    private final boolean revoked;

    /**
     *
     * Instantiates a new Certificate.
     *
     * @param info the certificate information
     * @param revoked the status indicating whether the certificate has been revoked
     */
    public Certificate(CertificateInfo info, boolean revoked) {
        this.info = info;
        this.revoked = revoked;
    }

    /**
     *
     * Instantiates a new Certificate with not revoked status.
     *
     * @param info the certificate information
     */
    public Certificate(CertificateInfo info) {
        this(info, false);
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
            throw new CertificateRevokedException(this.info);
        }

        return new Certificate(this.info, true);
    }

    public CertificateInfo getInfo() {
        return info;
    }

    public boolean isRevoked() {
        return revoked;
    }

}
