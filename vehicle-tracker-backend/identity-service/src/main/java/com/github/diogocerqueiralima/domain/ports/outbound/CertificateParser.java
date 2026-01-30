package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.model.CertificateInfo;

import java.util.Optional;

/**
 * Port to interact with the certificate parser implementation.
 */
public interface CertificateParser {

    /**
     *
     * Parses a certificate and extracts its information.
     * This method takes a Certificate object as input and returns a CertificateInfo object.
     * This method returns the certificate information, and it is not revoked by default.
     *
     * @param certificate the certificate to be parsed
     * @return an Optional containing the parsed CertificateInfo if successful, otherwise an empty Optional
     */
    Optional<CertificateInfo> parse(Certificate certificate);

}
