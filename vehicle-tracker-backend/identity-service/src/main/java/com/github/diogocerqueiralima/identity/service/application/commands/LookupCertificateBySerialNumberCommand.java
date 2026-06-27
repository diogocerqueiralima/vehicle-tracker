package com.github.diogocerqueiralima.identity.service.application.commands;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record LookupCertificateBySerialNumberCommand(@NotNull BigInteger serialNumber) {}
