package com.github.diogocerqueiralima.identity.service.application.commands;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CertificateSigningRequestCommand(
        @NotEmpty(message = "csr is required") byte[] value,
        @NotNull(message = "userId is required") UUID userId
) {}