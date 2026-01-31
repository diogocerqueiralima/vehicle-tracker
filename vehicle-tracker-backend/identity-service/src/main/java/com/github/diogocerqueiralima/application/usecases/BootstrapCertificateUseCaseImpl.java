package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.application.services.CertificateService;
import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;
import com.github.diogocerqueiralima.domain.ports.inbound.BootstrapCertificateUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.BootstrapCertificatePersistence;
import org.springframework.stereotype.Service;

@Service
public class BootstrapCertificateUseCaseImpl implements BootstrapCertificateUseCase {

    private final CertificateService certificateService;
    private final BootstrapCertificatePersistence bootstrapCertificatePersistence;

    public BootstrapCertificateUseCaseImpl(
            CertificateService certificateService, BootstrapCertificatePersistence bootstrapCertificatePersistence
    ) {
        this.certificateService = certificateService;
        this.bootstrapCertificatePersistence = bootstrapCertificatePersistence;
    }

    @Override
    public CertificateSigningRequestResult sign(CertificateSigningRequestCommand command) {
        return certificateService.sign(
                command,
                bootstrapCertificatePersistence::save,
                info -> new BootstrapCertificate(info, false, false)
        );
    }

}
