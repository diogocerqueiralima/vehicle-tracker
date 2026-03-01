package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotEmpty;

public record CertificateSigningRequestCommand(@NotEmpty byte[] value) {}