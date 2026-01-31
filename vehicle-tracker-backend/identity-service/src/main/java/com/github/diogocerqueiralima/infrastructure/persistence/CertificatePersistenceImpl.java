package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificatePersistence;
import com.github.diogocerqueiralima.infrastructure.entities.CertificateEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.CertificateMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.CertificateRepository;
import org.springframework.stereotype.Component;

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
    public Certificate save(Certificate certificate) {

        CertificateEntity entity = certificateMapper.toEntity(certificate);
        CertificateEntity savedEntity = certificateRepository.save(entity);

        return certificateMapper.toDomain(savedEntity);
    }
}
