package com.github.diogocerqueiralima.domain.exceptions;

import java.math.BigInteger;

/**
 * Exception thrown when an operation is attempted on a certificate that has already been revoked.
 */
public class CertificateRevokedException extends RuntimeException {

    public CertificateRevokedException(BigInteger serialNumber) {
        super("Certificate with serial number " + serialNumber + " has already been revoked.");
    }

}
