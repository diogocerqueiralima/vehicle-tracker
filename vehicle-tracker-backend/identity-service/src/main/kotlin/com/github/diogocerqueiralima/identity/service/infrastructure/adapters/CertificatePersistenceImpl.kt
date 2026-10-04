package com.github.diogocerqueiralima.identity.service.infrastructure.adapters

import com.github.diogocerqueiralima.identity.service.domain.model.certificate.RegularCertificate
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.CertificatePersistence
import com.github.diogocerqueiralima.identity.service.infrastructure.mappers.CertificateMapper
import com.github.diogocerqueiralima.identity.service.infrastructure.repositories.CertificateRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class CertificatePersistenceImpl(
    private val certificateMapper: CertificateMapper,
    private val certificateRepository: CertificateRepository
) : CertificatePersistence {

    override fun save(certificate: RegularCertificate): RegularCertificate {

        val entity = certificateMapper.toEntity(certificate)
        val savedEntity = certificateRepository.save(entity)

        return certificateMapper.toDomain(savedEntity)
    }

    override fun getBySerialNumber(serialNumber: BigInteger): RegularCertificate? =
        certificateRepository.findByIdOrNull(serialNumber)?.let(certificateMapper::toDomain)

}
