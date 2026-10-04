package com.github.diogocerqueiralima.identity.service.application.commands

import jakarta.validation.constraints.NotBlank
import java.util.UUID

/**
 * @property value the certificate signing request, PEM encoded
 * @property userId the unique identifier of the user making the request
 */
data class CertificateSigningRequestCommand(
    @get:NotBlank(message = "csr is required") val value: String,
    val userId: UUID
)
