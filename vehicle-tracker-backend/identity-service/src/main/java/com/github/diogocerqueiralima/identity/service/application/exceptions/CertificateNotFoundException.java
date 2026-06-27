package com.github.diogocerqueiralima.identity.service.application.exceptions;

import com.github.diogocerqueiralima.asset.service.error.common.exceptions.NotFoundException;

import java.math.BigInteger;

/**
 * Exception thrown when a certificate with the specified serial number is not found.
 */
public class CertificateNotFoundException extends NotFoundException {

    /**
     *
     * Constructs a new {@link CertificateNotFoundException} with a message indicating the missing certificate's serial number.
     *
     * @param serialNumber the serial number of the certificate that was not found
     */
    public CertificateNotFoundException(BigInteger serialNumber) {
        super("Certificate with serial number " + serialNumber + " not found.");
    }

}
