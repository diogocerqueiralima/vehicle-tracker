package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;
import com.github.diogocerqueiralima.domain.ports.outbound.BootstrapCertificatePersistence;
import com.github.diogocerqueiralima.infrastructure.entities.BootstrapCertificateEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.BootstrapCertificateMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.BootstrapCertificateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BootstrapCertificatePersistenceImpl implements BootstrapCertificatePersistence {

    private final BootstrapCertificateMapper bootstrapCertificateMapper;
    private final BootstrapCertificateRepository certificateRepository;

    public BootstrapCertificatePersistenceImpl(
            BootstrapCertificateMapper bootstrapCertificateMapper, BootstrapCertificateRepository certificateRepository
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

    @Override
    public Optional<BootstrapCertificate> getBySerialNumber(String serialNumber) {
        return certificateRepository
                .findById(serialNumber)
                .map(bootstrapCertificateMapper::toDomain);
    }

    @Override
    public Page<BootstrapCertificate> getPage(int page, int pageSize) {

        PageRequest pageRequest = PageRequest.of(page, pageSize);

        return certificateRepository
                .findAll(pageRequest)
                .map(bootstrapCertificateMapper::toDomain);
    }

}
