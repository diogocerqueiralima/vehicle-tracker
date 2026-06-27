package com.github.diogocerqueiralima.identity.service.domain.exceptions;

import com.github.diogocerqueiralima.asset.service.error.common.exceptions.OperationFailedException;

import java.math.BigInteger;

/**
 * Exception thrown when an operation is attempted on a certificate that could not be revoked
 */
public class CertificateRevokedException extends OperationFailedException {

    public CertificateRevokedException(BigInteger serialNumber) {
        super("Certificate with serial number " + serialNumber + " could not be revoked.");
    }

}
