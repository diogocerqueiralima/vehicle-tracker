package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record LookupCertificateBySerialNumberCommand(@NotNull BigInteger serialNumber) {}
