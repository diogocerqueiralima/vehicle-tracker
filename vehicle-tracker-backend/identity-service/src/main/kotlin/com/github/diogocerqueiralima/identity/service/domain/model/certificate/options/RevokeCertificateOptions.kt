package com.github.diogocerqueiralima.identity.service.domain.model.certificate.options

/**
 * Represents options for revoking a certificate.
 * This class is immutable and provides a method to validate the options.
 *
 * @property isRevoked true if the certificate has been marked as revoked, false otherwise
 */
open class RevokeCertificateOptions(val isRevoked: Boolean) : CertificateOptions {

    /**
     * Check if the certificate is not already revoked.
     *
     * @return true if the certificate is not revoked, false if it is already revoked.
     */
    override fun validate(): Boolean = !isRevoked

}
