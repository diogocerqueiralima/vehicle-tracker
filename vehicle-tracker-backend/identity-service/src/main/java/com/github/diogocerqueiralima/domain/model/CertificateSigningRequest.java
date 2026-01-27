package com.github.diogocerqueiralima.domain.model;

/**
 * Represents a Certificate Signing Request (CSR).
 */
public class CertificateSigningRequest {

    private final byte[] data;

    /**
     *
     * Initializes a new CertificateSigningRequest with the given data.
     *
     * @param data the byte array representing the CSR data
     */
    public CertificateSigningRequest(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

}
