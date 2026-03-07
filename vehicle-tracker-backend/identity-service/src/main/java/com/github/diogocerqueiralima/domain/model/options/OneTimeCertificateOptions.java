package com.github.diogocerqueiralima.domain.model.options;

/**
 * Represents options for a one-time certificate, which can be used only once.
 * This class extends {@link RevokeCertificateOptions} to include the revoked state, but also adds a used state.
 */
public class OneTimeCertificateOptions extends RevokeCertificateOptions {

    private final boolean used;

    public OneTimeCertificateOptions(boolean revoked, boolean used) {
        super(revoked);
        this.used = used;
    }

    /**
     *
     * Check if the certificate is not revoked and has not been used yet.
     *
     * @return true if the certificate is valid (not revoked and not used), false otherwise.
     */
    @Override
    public boolean validate() {
        return super.validate() && !this.used;
    }

    /**
     * @return true if the certificate has been marked as used, false otherwise.
     */
    public boolean isUsed() {
        return used;
    }

}
