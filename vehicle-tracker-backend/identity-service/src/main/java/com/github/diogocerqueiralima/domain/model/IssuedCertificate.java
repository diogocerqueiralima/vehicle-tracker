package com.github.diogocerqueiralima.domain.model;

/**
 * Represents a Certificate signed by a Certificate Authority (CA).
 */
public class IssuedCertificate {

    private final byte[] data;

    /**
     *
     * Initializes a new Certificate with the given data.
     *
     * @param data the byte array representing the certificate data
     */
    public IssuedCertificate(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

}
