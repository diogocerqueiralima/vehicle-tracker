package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotBlank;

public record MarkBootstrapCertificateAsUsedCommand(@NotBlank String serialNumber) {}
