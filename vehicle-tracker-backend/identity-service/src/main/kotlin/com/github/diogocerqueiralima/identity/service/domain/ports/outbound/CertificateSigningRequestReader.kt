package com.github.diogocerqueiralima.identity.service.domain.ports.outbound

import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSigningRequest

/**
 * Port to interact with the certificate signing request reader implementation.
 */
interface CertificateSigningRequestReader {

    /**
     * Reads a certificate signing request (CSR) from the provided PEM encoded items and constructs a CertificateSigningRequest object.
     *
     * @param data The PEM encoded certificate signing request.
     * @return A CertificateSigningRequest object representing the parsed CSR.
     */
    fun read(data: String): CertificateSigningRequest

}
