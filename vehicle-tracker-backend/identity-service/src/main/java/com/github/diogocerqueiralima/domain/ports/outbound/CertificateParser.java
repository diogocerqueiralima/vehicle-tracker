package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.Certificate;
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
     * @param certificate the certificate to be parsed
     * @return the parsed certificate information
     */
    CertificateInfo parse(Certificate certificate);

}
