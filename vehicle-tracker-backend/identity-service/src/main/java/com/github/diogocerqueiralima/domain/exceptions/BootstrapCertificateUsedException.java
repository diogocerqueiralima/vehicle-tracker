package com.github.diogocerqueiralima.domain.exceptions;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;

/**
 * Exception thrown when an operation is attempted on a bootstrap certificate that could not be used
 */
public class BootstrapCertificateUsedException extends RuntimeException {

    public BootstrapCertificateUsedException(BootstrapCertificate certificate) {
        super("Bootstrap Certificate with serial number " + certificate.getSerialNumber() + " could not be used.");
    }

}
