package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;

/**
 * Port to interact with the certificate signing request reader implementation.
 */
public interface CertificateSigningRequestReader {

    /**
     *
     * Reads a certificate signing request (CSR) from the provided byte array items and constructs a CertificateSigningRequest object.
     *
     * @param data A byte array containing the raw items of the certificate signing request.
     * @return A CertificateSigningRequest object representing the parsed CSR.
     */
    CertificateSigningRequest read(byte[] data);

}
