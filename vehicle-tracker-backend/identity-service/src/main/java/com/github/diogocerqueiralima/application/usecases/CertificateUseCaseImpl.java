package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.application.services.CertificateService;
import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.model.CertificateInfo;
import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.IssuedCertificate;
import com.github.diogocerqueiralima.domain.ports.inbound.CertificateUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateParser;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificatePersistence;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CertificateUseCaseImpl implements CertificateUseCase {

    private final CertificateService certificateService;
    private final CertificatePersistence certificatePersistence;

    public CertificateUseCaseImpl(CertificateService certificateService, CertificatePersistence certificatePersistence) {
        this.certificateService = certificateService;
        this.certificatePersistence = certificatePersistence;
    }

    @Override
    public CertificateSigningRequestResult sign(CertificateSigningRequestCommand command) {
        return certificateService.sign(
                command,
                certificatePersistence::save,
                info -> new Certificate(info, false)
        );
    }

}
