package com.github.diogocerqueiralima.identity.service.application.usecases;

import com.github.diogocerqueiralima.identity.service.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.identity.service.application.commands.LookupCertificateBySerialNumberCommand;
import com.github.diogocerqueiralima.identity.service.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.identity.service.application.services.CertificateService;
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.factories.RegularCertificateFactory;
import com.github.diogocerqueiralima.identity.service.domain.ports.inbound.CertificateUseCase;
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.CertificatePersistence;
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
                new RegularCertificateFactory()
        );
    }

    @Override
    public void revoke(LookupCertificateBySerialNumberCommand command) {

        certificateService.revoke(
                command,
                certificatePersistence::getBySerialNumber,
                certificatePersistence::save
        );

    }

}
