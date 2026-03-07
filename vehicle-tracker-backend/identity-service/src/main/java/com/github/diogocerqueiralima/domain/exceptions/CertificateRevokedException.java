package com.github.diogocerqueiralima.domain.exceptions;

import java.math.BigInteger;

/**
 * Exception thrown when an operation is attempted on a certificate that could not be revoked
 */
public class CertificateRevokedException extends RuntimeException {

    public CertificateRevokedException(BigInteger serialNumber) {
        super("Certificate with serial number " + serialNumber + " could not be revoked.");
    }

}
