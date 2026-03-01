package com.github.diogocerqueiralima.domain.model;

import java.math.BigInteger;
import java.time.Instant;

public interface CertificateFactory<T extends Certificate> {

    T create(CertificateSigningRequest csr, BigInteger serialNumber, Instant notBefore, Instant notAfter);

}
