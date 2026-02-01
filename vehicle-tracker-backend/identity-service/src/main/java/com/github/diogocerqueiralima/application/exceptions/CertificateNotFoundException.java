package com.github.diogocerqueiralima.application.exceptions;

public class CertificateNotFoundException extends RuntimeException {

    public CertificateNotFoundException(String serialNumber) {
        super("Certificate with serial number " + serialNumber + " not found.");
    }

}
