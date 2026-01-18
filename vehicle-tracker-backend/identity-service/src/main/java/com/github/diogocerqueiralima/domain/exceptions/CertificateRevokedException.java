package com.github.diogocerqueiralima.domain.exceptions;

import com.github.diogocerqueiralima.domain.model.Certificate;

/**
 * Exception thrown when an operation is attempted on a certificate that has already been revoked.
 */
public class CertificateRevokedException extends RuntimeException {

    public CertificateRevokedException(Certificate certificate) {
        super("Certificate with serial number " + certificate.getSerialNumber() + " has already been revoked.");
    }

}
