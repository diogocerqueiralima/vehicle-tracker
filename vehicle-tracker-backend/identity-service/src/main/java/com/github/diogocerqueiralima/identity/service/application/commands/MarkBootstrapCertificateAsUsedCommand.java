package com.github.diogocerqueiralima.identity.service.application.commands;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record MarkBootstrapCertificateAsUsedCommand(@NotNull BigInteger serialNumber) {}
