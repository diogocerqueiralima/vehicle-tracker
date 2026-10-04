package com.github.diogocerqueiralima.identity.service.infrastructure.mappers

import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSubject
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.RegularCertificate
import com.github.diogocerqueiralima.identity.service.infrastructure.entities.CertificateEntity
import org.springframework.stereotype.Component

@Component
class CertificateMapper {

    /**
     * Converts a [CertificateEntity] to a [RegularCertificate] domain object.
     *
     * @param entity the [CertificateEntity] to be converted
     * @return a [RegularCertificate] domain object corresponding to the provided entity
     */
    fun toDomain(entity: CertificateEntity): RegularCertificate =
        RegularCertificate(
            entity.serialNumber,
            CertificateSubject.fromString(entity.subject),
            entity.issuedAt,
            entity.expiresAt,
            entity.revoked
        )

    /**
     * Converts a [RegularCertificate] domain object to a [CertificateEntity].
     *
     * @param certificate the [RegularCertificate] to be converted
     * @return a [CertificateEntity] corresponding to the provided domain object
     */
    fun toEntity(certificate: RegularCertificate): CertificateEntity =
        CertificateEntity().apply {
            serialNumber = certificate.serialNumber
            subject = certificate.subject.toString()
            issuedAt = certificate.issuedAt
            expiresAt = certificate.expiresAt
            revoked = certificate.isRevoked
        }

}
