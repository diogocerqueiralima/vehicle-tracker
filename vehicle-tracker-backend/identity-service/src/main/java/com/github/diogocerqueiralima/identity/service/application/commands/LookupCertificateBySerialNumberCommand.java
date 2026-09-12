package com.github.diogocerqueiralima.identity.service.application.commands;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Command to look a certificate up by its serial number, on behalf of the user making the request.
 *
 * @param serialNumber the serial number of the certificate to look up
 * @param userId the user the request is made on behalf of, whose ownership of the device the
 * certificate was issued to decides whether the operation is allowed
 */
public record LookupCertificateBySerialNumberCommand(@NotNull BigInteger serialNumber, @NotNull UUID userId) {}
