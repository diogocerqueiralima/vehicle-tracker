package com.github.diogocerqueiralima.identity.service.application.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class CertificateAuthorityConfig(
    @Value("\${ca.issuer}") val issuer: String,
    @Value("\${ca.validity.days}") val validityDays: Int
)
