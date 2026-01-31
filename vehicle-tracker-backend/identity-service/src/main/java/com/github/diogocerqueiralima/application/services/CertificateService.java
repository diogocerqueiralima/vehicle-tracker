package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.model.CertificateInfo;
import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.IssuedCertificate;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateParser;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateService.class);
    private final CertificateSigner certificateSigner;
    private final CertificateParser certificateParser;

    public CertificateService(CertificateSigner certificateSigner, CertificateParser certificateParser) {
        this.certificateSigner = certificateSigner;
        this.certificateParser = certificateParser;
    }

    /**
     *
     * Signs a certificate signing request and persists the resulting certificate.
     *
     * @param command the certificate signing request command containing the CSR details
     * @param persistence a function to persist the Certificate
     * @param factory a function to create a Certificate from CertificateInfo
     * @return the result of the certificate signing request, including the signed certificate data
     */
    public <T extends Certificate> CertificateSigningRequestResult sign(
            CertificateSigningRequestCommand command,
            Function<T, T> persistence,
            Function<CertificateInfo, T> factory
    ) {

        // 1. Sign certificate

        CertificateSigningRequest request = new CertificateSigningRequest(command.value());
        IssuedCertificate issuedCertificate = certificateSigner.sign(request);

        // 2. Parse certificate information

        CertificateInfo certificateInfo = certificateParser.parse(issuedCertificate);

        log.info(
                "Parsed certificate info: SerialNumber={}, Subject={}",
                certificateInfo.getSerialNumber(),
                certificateInfo.getSubject()
        );

        // 3. Create and persists the certificate

        Certificate certificate = persistence.apply(factory.apply(certificateInfo));

        log.info("Persisted certificate with SerialNumber={}", certificate.getInfo().getSerialNumber());

        // 4. Build result

        return new CertificateSigningRequestResult(
                certificate.getInfo().getSerialNumber(),
                certificate.getInfo().getSubject(),
                issuedCertificate.getData()
        );
    }

}
