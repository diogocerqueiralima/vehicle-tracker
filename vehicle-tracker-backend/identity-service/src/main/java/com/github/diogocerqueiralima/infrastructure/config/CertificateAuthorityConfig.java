package com.github.diogocerqueiralima.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CertificateAuthorityConfig {

    @Value("${ca.issuer}")
    private String issuer;

    @Value("${ca.validity.days}")
    private int validityDays;

    public String getIssuer() {
        return issuer;
    }

    public int getValidityDays() {
        return validityDays;
    }

}
