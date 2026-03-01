package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.RegularCertificate;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificatePersistence;
import com.github.diogocerqueiralima.infrastructure.entities.CertificateEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.CertificateMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.CertificateRepository;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Optional;

@Component
public class CertificatePersistenceImpl implements CertificatePersistence {

    private final CertificateMapper certificateMapper;
    private final CertificateRepository certificateRepository;

    public CertificatePersistenceImpl(
            CertificateMapper certificateMapper, CertificateRepository certificateRepository
    ) {
        this.certificateMapper = certificateMapper;
        this.certificateRepository = certificateRepository;
    }

    @Override
    public RegularCertificate save(RegularCertificate certificate) {

        CertificateEntity entity = certificateMapper.toEntity(certificate);
        CertificateEntity savedEntity = certificateRepository.save(entity);

        return certificateMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<RegularCertificate> getBySerialNumber(BigInteger serialNumber) {
        return certificateRepository
                .findById(serialNumber)
                .map(certificateMapper::toDomain);
    }

}
