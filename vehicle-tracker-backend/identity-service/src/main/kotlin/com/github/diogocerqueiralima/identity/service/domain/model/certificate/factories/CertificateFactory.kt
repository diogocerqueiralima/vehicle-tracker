package com.github.diogocerqueiralima.identity.service.domain.model.certificate.factories

import com.github.diogocerqueiralima.identity.service.domain.model.certificate.Certificate
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSigningRequest
import java.math.BigInteger
import java.time.Instant

/**
 * Factory interface for creating Certificate instances.
 *
 * @param T the type of Certificate to be created
 */
interface CertificateFactory<T : Certificate> {

    /**
     * Creates a new Certificate instance based on the provided [CertificateSigningRequest] and other parameters.
     *
     * @param csr the Certificate Signing Request containing the subject and public key information
     * @param serialNumber the unique serial number to be assigned to the certificate
     * @param notBefore the timestamp indicating when the certificate becomes valid
     * @param notAfter the timestamp indicating when the certificate expires
     * @return a new instance of type T that extends Certificate, initialized with the provided parameters
     */
    fun create(csr: CertificateSigningRequest, serialNumber: BigInteger, notBefore: Instant, notAfter: Instant): T

}
