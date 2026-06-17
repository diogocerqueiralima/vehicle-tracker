package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

public record CertificateSigningRequestCommand(@NotEmpty byte[] value, @NotBlank UUID userId) {}