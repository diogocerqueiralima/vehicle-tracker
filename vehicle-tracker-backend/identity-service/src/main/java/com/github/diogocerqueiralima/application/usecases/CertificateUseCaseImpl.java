package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.domain.model.CertificateInfo;
import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.ports.inbound.CertificateUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateParser;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateInfoPersistence;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CertificateUseCaseImpl implements CertificateUseCase {

    private static final Logger log = LoggerFactory.getLogger(CertificateUseCaseImpl.class);

    private final CertificateSigner certificateSigner;
    private final CertificateParser certificateParser;
    private final CertificateInfoPersistence certificateInfoPersistence;

    public CertificateUseCaseImpl(
            CertificateSigner certificateSigner, CertificateParser certificateParser, CertificateInfoPersistence certificateInfoPersistence
    ) {
        this.certificateSigner = certificateSigner;
        this.certificateParser = certificateParser;
        this.certificateInfoPersistence = certificateInfoPersistence;
    }

    @Override
    public CertificateSigningRequestResult sign(CertificateSigningRequestCommand command) {

        // 1. Sign certificate

        CertificateSigningRequest request = new CertificateSigningRequest(command.value());
        Certificate certificate = certificateSigner.sign(request);

        // 2. Parse and persist certificate info

        CertificateInfo certificateInfo = certificateInfoPersistence.save(certificateParser.parse(certificate));

        // 3. Build and return the result

        CertificateSigningRequestResult result = new CertificateSigningRequestResult(
                certificateInfo.getSerialNumber(),
                certificateInfo.getSubject(),
                certificateInfo.getIssuedAt(),
                certificateInfo.getExpiresAt(),
                certificateInfo.isRevoked(),
                certificate.getData()
        );

        log.info(
                "Certificate parsed successfully: Serial Number - {}, Subject - {}",
                result.serialNumber(), result.subject()
        );

        return result;
    }

}
