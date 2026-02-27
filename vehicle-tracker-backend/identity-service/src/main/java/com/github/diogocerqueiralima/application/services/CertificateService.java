package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.commands.LookupCertificateBySerialNumberCommand;
import com.github.diogocerqueiralima.application.config.CertificateAuthorityConfig;
import com.github.diogocerqueiralima.application.exceptions.CertificateNotFoundException;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.model.CertificateFactory;
import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigner;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigningRequestReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

@Service
public class CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateService.class);

    private final CertificateSigner certificateSigner;
    private final CertificateSigningRequestReader certificateSigningRequestReader;
    private final CertificateAuthorityConfig certificateAuthorityConfig;

    public CertificateService(
            CertificateSigner certificateSigner,
            CertificateSigningRequestReader certificateSigningRequestReader,
            CertificateAuthorityConfig certificateAuthorityConfig
    ) {
        this.certificateSigner = certificateSigner;
        this.certificateSigningRequestReader = certificateSigningRequestReader;
        this.certificateAuthorityConfig = certificateAuthorityConfig;
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

        // 2. Generates the serial number, notBefore and notAfter timestamps for the certificate based on the CA configuration
        BigInteger serialNumber = generateSerialNumber();
        Instant notBefore = Instant.now();
        Instant notAfter = notBefore.plusSeconds(
                certificateAuthorityConfig.getValidityDays() * 24L * 60L * 60L
        );

        // 3. Sign certificate
        byte[] certificateData = certificateSigner.sign(
                certificateSigningRequest,
                certificateAuthorityConfig.getIssuer(),
                generateSerialNumber(),
                notBefore,
                notAfter
        );

        // 4. Create and persists the certificate
        Certificate certificate = persistence.apply(
                factory.create(certificateSigningRequest, serialNumber, notBefore, notAfter)
        );
        log.info("Persisted certificate with SerialNumber={}", certificate.getSerialNumber());

        // 5. Build result
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
            Function<String, Optional<T>> finder,
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

}
