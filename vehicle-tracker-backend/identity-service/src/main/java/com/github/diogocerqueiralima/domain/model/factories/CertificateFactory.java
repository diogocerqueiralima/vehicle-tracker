package com.github.diogocerqueiralima.domain.model.factories;

import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;

import java.math.BigInteger;
import java.time.Instant;

/**
 *
 * Factory interface for creating Certificate instances.
 *
 * @param <T> the type of Certificate to be created
 */
public interface CertificateFactory<T extends Certificate> {

    /**
     *
     * Creates a new Certificate instance based on the provided {@link CertificateSigningRequest} and other parameters.
     *
     * @param csr the Certificate Signing Request containing the subject and public key information
     * @param serialNumber the unique serial number to be assigned to the certificate
     * @param notBefore the timestamp indicating when the certificate becomes valid
     * @param notAfter the timestamp indicating when the certificate expires
     * @return a new instance of type T that extends Certificate, initialized with the provided parameters
     */
    T create(CertificateSigningRequest csr, BigInteger serialNumber, Instant notBefore, Instant notAfter);

}
