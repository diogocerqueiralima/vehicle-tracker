package com.github.diogocerqueiralima.domain.model.options;

/**
 * Represents options for revoking a certificate.
 * This class is immutable and provides a method to validate the options.
 */
public class RevokeCertificateOptions implements CertificateOptions {

    private final boolean revoked;

    public RevokeCertificateOptions(boolean revoked) {
        this.revoked = revoked;
    }

    /**
     *
     * Check if the certificate is not already revoked.
     *
     * @return true if the certificate is not revoked, false if it is already revoked.
     */
    @Override
    public boolean validate() {
        return !this.revoked;
    }

    /**
     * @return true if the certificate has been marked as revoked, false otherwise.
     */
    public boolean isRevoked() {
        return revoked;
    }

}
