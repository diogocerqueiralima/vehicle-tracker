package com.github.diogocerqueiralima.identity.service.domain.model.certificate.options

/**
 * Interface representing options for certificate operations, such as revocation or usage.
 * Implementations of this interface should provide validation logic to ensure that the options are valid for the intended operation.
 */
interface CertificateOptions {

    /**
     * Validates the options to ensure they are suitable for the intended certificate operation.
     *
     * @return true if the options are valid for the intended operation, false otherwise.
     */
    fun validate(): Boolean

}
