package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Port to interact with the certificate signer implementation.
 */
public interface CertificateSigner {

    /**
     *
     * Signs a certificate signing request (CSR) and issues a certificate.
     *
     * @param csr The CertificateSigningRequest object containing the details of the certificate to be signed.
     * @param issuer The name of the issuer (CA) that will be associated with the issued certificate.
     * @param serialNumber A unique serial number to be assigned to the issued certificate.
     * @param notBefore The timestamp indicating when the issued certificate becomes valid.
     * @param notAfter The timestamp indicating when the issued certificate expires.
     * @return A byte array representing the signed certificate items, which can be stored or transmitted as needed.
     */
    byte[] sign(CertificateSigningRequest csr, String issuer, BigInteger serialNumber, Instant notBefore, Instant notAfter);

}
