package com.github.diogocerqueiralima.identity.service.domain.model.certificate.options

/**
 * Represents options for a one-time certificate, which can be used only once.
 * This class extends [RevokeCertificateOptions] to include the revoked state, but also adds a used state.
 *
 * @property isUsed true if the certificate has been marked as used, false otherwise
 */
class OneTimeCertificateOptions(revoked: Boolean, val isUsed: Boolean) : RevokeCertificateOptions(revoked) {

    /**
     * Check if the certificate is not revoked and has not been used yet.
     *
     * @return true if the certificate is valid (not revoked and not used), false otherwise.
     */
    override fun validate(): Boolean = super.validate() && !isUsed

}
