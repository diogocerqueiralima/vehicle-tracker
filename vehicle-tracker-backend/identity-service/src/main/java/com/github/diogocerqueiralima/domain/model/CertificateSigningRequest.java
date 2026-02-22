package com.github.diogocerqueiralima.domain.model;

import java.security.PublicKey;

/**
 * Represents a Certificate Signing Request (CSR).
 */
public class CertificateSigningRequest {

    private final CertificateSubject subject;
    private final PublicKey publicKey;

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
