package com.github.diogocerqueiralima.domain.exceptions;

import com.github.diogocerqueiralima.domain.model.CertificateInfo;

/**
 * Exception thrown when an operation is attempted on a certificate that has already been revoked.
 */
public class CertificateRevokedException extends RuntimeException {

    public CertificateRevokedException(CertificateInfo certificateInfo) {
        super("Certificate with serial number " + certificateInfo.getSerialNumber() + " has already been revoked.");
    }

}
