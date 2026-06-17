package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.commands.LookupCertificateBySerialNumberCommand;
import com.github.diogocerqueiralima.application.config.CertificateAuthorityConfig;
import com.github.diogocerqueiralima.application.exceptions.CertificateNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotOwnedException;
import com.github.diogocerqueiralima.application.exceptions.InvalidCertificateSubjectException;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.domain.model.certificate.Certificate;
import com.github.diogocerqueiralima.domain.model.certificate.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.certificate.CertificateSubject;
import com.github.diogocerqueiralima.domain.model.certificate.factories.CertificateFactory;
import com.github.diogocerqueiralima.domain.model.device.Device;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigner;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigningRequestReader;
import com.github.diogocerqueiralima.domain.ports.outbound.DeviceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
public class CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateService.class);

    private final CertificateSigner certificateSigner;
    private final CertificateSigningRequestReader certificateSigningRequestReader;
    private final CertificateAuthorityConfig certificateAuthorityConfig;
    private final DeviceProvider deviceProvider;

    public CertificateService(
            CertificateSigner certificateSigner,
            CertificateSigningRequestReader certificateSigningRequestReader,
            CertificateAuthorityConfig certificateAuthorityConfig,
            DeviceProvider deviceProvider
    ) {
        this.certificateSigner = certificateSigner;
        this.certificateSigningRequestReader = certificateSigningRequestReader;
        this.certificateAuthorityConfig = certificateAuthorityConfig;
        this.deviceProvider = deviceProvider;
    }

    /**
     *
     * Signs a certificate signing request and persists the resulting certificate.
     *
     * @param command the certificate signing request command containing the CSR details
     * @param persistence a function to persist the Certificate
     * @param factory a function to create a Certificate from CertificateInfo
     * @return the result of the certificate signing request, including the signed certificate items
     */
    public <T extends Certificate> CertificateSigningRequestResult sign(
            CertificateSigningRequestCommand command,
            Function<T, T> persistence,
            CertificateFactory<T> factory
    ) {

        // 1. Parses the certificate signing request command into a CertificateSigningRequest object
        CertificateSigningRequest certificateSigningRequest = certificateSigningRequestReader.read(command.value());
        log.info("Parsed certificate signing request for Subject={}", certificateSigningRequest.getSubject());

        // 2. Validates that the certificate subject contains a valid device ID.
        UUID deviceId = getDeviceIdFromCertificateSubject(certificateSigningRequest.getSubject())
                .orElseThrow(() ->
                        new InvalidCertificateSubjectException("Common Name must be a valid UUID representing the device ID")
                );

        // 3. Validates that the device exists and is owned by the user making the request.
        Device device = deviceProvider.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        // 4. Validates that the user making the request is the owner of the device.
        if (!device.getOwnerId().equals(command.userId())) {
            throw new DeviceNotOwnedException(deviceId, command.userId());
        }

        // 5. Generates the serial number, notBefore and notAfter timestamps for the certificate based on the CA configuration
        BigInteger serialNumber = generateSerialNumber();
        Instant notBefore = Instant.now();
        Instant notAfter = notBefore.plusSeconds(
                certificateAuthorityConfig.getValidityDays() * 24L * 60L * 60L
        );

        // 6. Sign certificate
        byte[] certificateData = certificateSigner.sign(
                certificateSigningRequest,
                certificateAuthorityConfig.getIssuer(),
                serialNumber,
                notBefore,
                notAfter
        );

        // 7. Create and persists the certificate
        Certificate certificate = persistence.apply(
                factory.create(certificateSigningRequest, serialNumber, notBefore, notAfter)
        );
        log.info("Persisted certificate with SerialNumber={}", certificate.getSerialNumber());

        // 8. Build result
        return new CertificateSigningRequestResult(
                certificate.getSerialNumber(),
                certificate.getSubject().toString(),
                certificateData
        );

    }

    /**
     *
     * Revokes a certificate by its serial number. It looks up the certificate using the provided finder function,
     * and if found, it revokes the certificate and persists the updated state using the provided persistence function.
     *
     * @param command the command containing the serial number of the certificate to be revoked
     * @param finder a function to find the certificate by its serial number, returning an Optional of the certificate
     * @param persistence a function to persist the revoked certificate
     */
    @SuppressWarnings("unchecked")
    public <T extends Certificate> void revoke(
            LookupCertificateBySerialNumberCommand command,
            Function<BigInteger, Optional<T>> finder,
            Function<T, T> persistence
    ) {
        T certificate = finder.apply(command.serialNumber())
                .orElseThrow(() -> new CertificateNotFoundException(command.serialNumber()));

        persistence.apply((T) certificate.revoke());
    }

    /**
     *
     * Generates a random serial number for the certificate.
     *
     * @return the generated serial number
     */
    private BigInteger generateSerialNumber() {
        return new BigInteger(120, new SecureRandom());
    }

    /**
     *
     * Extracts the device ID from the certificate subject's common name. It attempts to parse the common name as a UUID.
     *
     * @param subject the certificate subject containing the common name to be parsed
     * @return an Optional containing the device ID if parsing is successful, or an empty Optional if parsing fails
     */
    private Optional<UUID> getDeviceIdFromCertificateSubject(CertificateSubject subject) {

        try {
            return Optional.of(UUID.fromString(subject.getCommonName()));
        } catch (Exception e) {
            return Optional.empty();
        }

    }

}
