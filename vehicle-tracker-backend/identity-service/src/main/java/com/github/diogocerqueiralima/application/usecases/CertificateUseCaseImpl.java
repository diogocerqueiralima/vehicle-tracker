package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.commands.LookupCertificateBySerialNumberCommand;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.application.services.CertificateService;
import com.github.diogocerqueiralima.domain.model.RegularCertificate;
import com.github.diogocerqueiralima.domain.ports.inbound.CertificateUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificatePersistence;
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
                (csr, serialNumber, notBefore, notAfter) ->
                        new RegularCertificate(
                                serialNumber,
                                csr.getSubject(),
                                notBefore,
                                notAfter,
                                false
                        )
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
