package com.github.diogocerqueiralima.identity.service.domain.exceptions;

import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException;

import java.math.BigInteger;

/**
 * Exception thrown when a certificate revocation operation fails.
 */
public class CertificateRevocationFailedException extends OperationFailedException {

    public CertificateRevocationFailedException(BigInteger serialNumber) {
        super("Certificate with serial number " + serialNumber + " could not be revoked.");
    }

}
