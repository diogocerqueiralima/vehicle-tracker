package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.commands.LookupCertificateBySerialNumberCommand;
import com.github.diogocerqueiralima.application.commands.MarkBootstrapCertificateAsUsedCommand;
import com.github.diogocerqueiralima.application.commands.RetrievePageCommand;
import com.github.diogocerqueiralima.application.exceptions.CertificateNotFoundException;
import com.github.diogocerqueiralima.application.results.BootstrapCertificateResult;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.application.services.CertificateService;
import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;
import com.github.diogocerqueiralima.domain.ports.inbound.BootstrapCertificateUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.BootstrapCertificatePersistence;
import org.springframework.data.domain.Page;
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
                (csr, serialNumber, notBefore, notAfter) ->
                        new BootstrapCertificate(
                                serialNumber,
                                csr.getSubject(),
                                notBefore,
                                notAfter,
                                false,
                                false
                        )
        );
    }

    @Override
    public void markAsUsed(MarkBootstrapCertificateAsUsedCommand command) {

        BootstrapCertificate certificate = bootstrapCertificatePersistence
                .getBySerialNumber(command.serialNumber())
                .map(BootstrapCertificate::markAsUsed)
                .orElseThrow(() -> new CertificateNotFoundException(command.serialNumber()));

        bootstrapCertificatePersistence.save(certificate);
    }

    @Override
    public void revoke(LookupCertificateBySerialNumberCommand command) {

        certificateService.revoke(
                command,
                bootstrapCertificatePersistence::getBySerialNumber,
                bootstrapCertificatePersistence::save
        );

    }

    @Override
    public PageResult<BootstrapCertificateResult> getPage(RetrievePageCommand command) {

        Page<BootstrapCertificate> page = bootstrapCertificatePersistence
                .getPage(command.page() - 1, command.pageSize());

        return new PageResult<>(
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getTotalElements(),
                page
                        .map(bootstrapCertificate ->
                                new BootstrapCertificateResult(
                                        bootstrapCertificate.getSerialNumber(),
                                        bootstrapCertificate.getSubject().toString(),
                                        bootstrapCertificate.getIssuedAt(),
                                        bootstrapCertificate.getExpiresAt(),
                                        bootstrapCertificate.isRevoked(),
                                        bootstrapCertificate.isUsed()
                                )
                        )
                        .toList()
        );
    }

}
