package com.github.diogocerqueiralima.domain.model;

import java.security.PublicKey;

/**
 * Represents a Certificate Signing Request (CSR).
 *
 * @see CertificateSubject
 */
public class CertificateSigningRequest {

    private final CertificateSubject subject;
    private final PublicKey publicKey;

    /**
     * Creates a new Certificate Signing Request with the given subject and public key.
     *
     * @param subject the subject information for the certificate to be issued
     * @param publicKey the public key to be included in the certificate
     */
    public CertificateSigningRequest(CertificateSubject subject, PublicKey publicKey) {
        this.subject = subject;
        this.publicKey = publicKey;
    }

    public CertificateSubject getSubject() {
        return subject;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

}
