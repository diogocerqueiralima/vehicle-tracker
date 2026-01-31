package com.github.diogocerqueiralima.application.results;

public record CertificateSigningRequestResult(
        String serialNumber,
        String subject,
        byte[] data
) {}
