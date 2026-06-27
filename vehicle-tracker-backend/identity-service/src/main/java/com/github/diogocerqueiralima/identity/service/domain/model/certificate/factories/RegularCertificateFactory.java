package com.github.diogocerqueiralima.identity.service.domain.model.certificate.factories;

import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSigningRequest;
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.RegularCertificate;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Factory for creating RegularCertificate instances from CertificateSigningRequest data.
 */
public class RegularCertificateFactory implements CertificateFactory<RegularCertificate> {

    /**
     *
     * {@inheritDoc}
     *
     * @return A new instance of RegularCertificate.
     */
    @Override
    public RegularCertificate create(
            CertificateSigningRequest csr, BigInteger serialNumber, Instant notBefore, Instant notAfter
    ) {
        return new RegularCertificate(
                serialNumber,
                csr.getSubject(),
                notBefore,
                notAfter,
                false
        );
    }

}
