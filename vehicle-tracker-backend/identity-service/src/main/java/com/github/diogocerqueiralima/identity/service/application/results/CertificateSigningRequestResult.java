package com.github.diogocerqueiralima.identity.service.application.results;

import java.math.BigInteger;

public record CertificateSigningRequestResult(
        BigInteger serialNumber,
        String subject,
        byte[] data
) {}
