package com.github.diogocerqueiralima.domain.model.factories;

import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.RegularCertificate;

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
