package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.Certificate;

/**
 * Port to interact with the certificate signer implementation.
 */
public interface CertificateSigner {

    /**
     *
     * Signs a certificate signing request.
     * This method takes a CertificateSigningRequest object as input and returns a Certificate object.
     * To sign the certificate, it uses the CA's private key stored in a secure KeyStore.
     *
     * @param request the certificate signing request to be signed
     * @return the signed certificate response if the signing is successful, otherwise an empty Optional
     */
    Certificate sign(CertificateSigningRequest request);

}
