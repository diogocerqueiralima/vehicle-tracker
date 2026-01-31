package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;
import com.github.diogocerqueiralima.domain.ports.outbound.BootstrapCertificatePersistence;
import com.github.diogocerqueiralima.infrastructure.entities.BootstrapCertificateEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.BootstrapCertificateMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.CertificateRepository;
import org.springframework.stereotype.Component;

@Component
public class BootstrapCertificatePersistenceImpl implements BootstrapCertificatePersistence {

    private final BootstrapCertificateMapper bootstrapCertificateMapper;
    private final CertificateRepository certificateRepository;

    public BootstrapCertificatePersistenceImpl(
            BootstrapCertificateMapper bootstrapCertificateMapper, CertificateRepository certificateRepository
    ) {
        this.bootstrapCertificateMapper = bootstrapCertificateMapper;
        this.certificateRepository = certificateRepository;
    }

    @Override
    public BootstrapCertificate save(BootstrapCertificate certificate) {

        BootstrapCertificateEntity entity = bootstrapCertificateMapper.toEntity(certificate);
        BootstrapCertificateEntity savedEntity = certificateRepository.save(entity);

        return bootstrapCertificateMapper.toDomain(savedEntity);
    }

}
