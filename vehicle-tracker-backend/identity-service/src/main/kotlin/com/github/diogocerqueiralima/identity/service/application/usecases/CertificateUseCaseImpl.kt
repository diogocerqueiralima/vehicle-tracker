package com.github.diogocerqueiralima.identity.service.application.usecases

import com.github.diogocerqueiralima.identity.service.application.commands.CertificateSigningRequestCommand
import com.github.diogocerqueiralima.identity.service.application.commands.LookupCertificateBySerialNumberCommand
import com.github.diogocerqueiralima.identity.service.application.results.CertificateSigningRequestResult
import com.github.diogocerqueiralima.identity.service.application.services.CertificateService
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.factories.RegularCertificateFactory
import com.github.diogocerqueiralima.identity.service.domain.ports.inbound.CertificateUseCase
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.CertificatePersistence
import org.springframework.stereotype.Service

@Service
class CertificateUseCaseImpl(
    private val certificateService: CertificateService,
    private val certificatePersistence: CertificatePersistence
) : CertificateUseCase {

    override fun sign(command: CertificateSigningRequestCommand): CertificateSigningRequestResult =
        certificateService.sign(
            command,
            certificatePersistence::save,
            RegularCertificateFactory()
        )

    override fun revoke(command: LookupCertificateBySerialNumberCommand) {
        certificateService.revoke(
            command,
            certificatePersistence::getBySerialNumber,
            certificatePersistence::save
        )
    }

}
