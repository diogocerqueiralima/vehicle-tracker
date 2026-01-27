package com.github.diogocerqueiralima.domain.exceptions;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificateInfo;

public class BootstrapCertificateUsedException extends RuntimeException {

    public BootstrapCertificateUsedException(BootstrapCertificateInfo certificate) {
        super("Bootstrap Certificate with serial number " + certificate.getSerialNumber() + " has already been used.");
    }

}
