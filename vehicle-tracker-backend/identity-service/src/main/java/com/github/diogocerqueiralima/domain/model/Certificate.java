package com.github.diogocerqueiralima.domain.model;

/**
 * Represents a Certificate signed by a Certificate Authority (CA).
 */
public class Certificate {

    private final byte[] data;

    /**
     *
     * Initializes a new Certificate with the given data.
     *
     * @param data the byte array representing the certificate data
     */
    public Certificate(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

}
