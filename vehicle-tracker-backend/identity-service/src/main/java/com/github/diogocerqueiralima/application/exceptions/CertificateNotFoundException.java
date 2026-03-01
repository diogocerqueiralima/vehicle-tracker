package com.github.diogocerqueiralima.application.exceptions;

import java.math.BigInteger;

public class CertificateNotFoundException extends RuntimeException {

    public CertificateNotFoundException(BigInteger serialNumber) {
        super("Certificate with serial number " + serialNumber + " not found.");
    }

}
