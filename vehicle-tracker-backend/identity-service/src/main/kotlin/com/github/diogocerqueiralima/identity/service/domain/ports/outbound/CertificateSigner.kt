package com.github.diogocerqueiralima.identity.service.domain.ports.outbound

import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSigningRequest
import java.math.BigInteger
import java.time.Instant

/**
 * Port to interact with the certificate signer implementation.
 */
interface CertificateSigner {

    /**
     * Signs a certificate signing request (CSR) and issues a certificate.
     *
     * @param csr The CertificateSigningRequest object containing the details of the certificate to be signed.
     * @param issuer The name of the issuer (CA) that will be associated with the issued certificate.
     * @param serialNumber A unique serial number to be assigned to the issued certificate.
     * @param notBefore The timestamp indicating when the issued certificate becomes valid.
     * @param notAfter The timestamp indicating when the issued certificate expires.
     * @return The signed certificate, PEM encoded.
     */
    fun sign(csr: CertificateSigningRequest, issuer: String, serialNumber: BigInteger, notBefore: Instant, notAfter: Instant): String

}
