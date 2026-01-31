package com.github.diogocerqueiralima.domain.exceptions;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;

public class BootstrapCertificateUsedException extends RuntimeException {

    public BootstrapCertificateUsedException(BootstrapCertificate certificate) {
        super("Bootstrap Certificate with serial number " + certificate.getInfo().getSerialNumber() + " has already been used.");
    }

}
