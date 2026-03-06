package com.github.diogocerqueiralima.domain.model.factories;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;
import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Factory for creating BootstrapCertificate instances from CertificateSigningRequest data.
 */
public class BootstrapCertificateFactory implements CertificateFactory<BootstrapCertificate> {

    /**
     *
     * {@inheritDoc}
     *
     * @return A new instance of BootstrapCertificate.
     */
    @Override
    public BootstrapCertificate create(
            CertificateSigningRequest csr, BigInteger serialNumber, Instant notBefore, Instant notAfter
    ) {
        return new BootstrapCertificate(
                serialNumber,
                csr.getSubject(),
                notBefore,
                notAfter,
                false,
                false
        );
    }

}
