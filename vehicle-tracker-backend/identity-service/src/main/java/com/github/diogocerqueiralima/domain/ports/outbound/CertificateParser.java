package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.IssuedCertificate;
import com.github.diogocerqueiralima.domain.model.CertificateInfo;

/**
 * Port to interact with the certificate parser implementation.
 */
public interface CertificateParser {

    /**
     *
     * Parses a certificate and extracts its information.
     * This method takes a Certificate object as input and returns a CertificateInfo object.
     *
     * @param issuedCertificate the certificate to be parsed
     * @return an Optional containing the parsed CertificateInfo if successful, otherwise an empty Optional
     */
    CertificateInfo parse(IssuedCertificate issuedCertificate);

}
