package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotBlank;

public record LookupCertificateBySerialNumberCommand(@NotBlank String serialNumber) {}
