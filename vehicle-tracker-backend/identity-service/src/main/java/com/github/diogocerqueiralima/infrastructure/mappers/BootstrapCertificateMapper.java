package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;
import com.github.diogocerqueiralima.domain.model.CertificateSubject;
import com.github.diogocerqueiralima.infrastructure.entities.BootstrapCertificateEntity;
import org.springframework.stereotype.Component;

@Component
public class BootstrapCertificateMapper {


    public BootstrapCertificate toDomain(BootstrapCertificateEntity entity) {
        return new BootstrapCertificate(
                entity.getSerialNumber(),
                CertificateSubject.fromString(entity.getSubject()),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.isRevoked(),
                entity.isUsed()
        );
    }

    public BootstrapCertificateEntity toEntity(BootstrapCertificate certificate) {

        BootstrapCertificateEntity entity = new BootstrapCertificateEntity();

        entity.setSerialNumber(certificate.getSerialNumber());
        entity.setSubject(certificate.getSubject().toString());
        entity.setIssuedAt(certificate.getIssuedAt());
        entity.setExpiresAt(certificate.getExpiresAt());
        entity.setRevoked(certificate.isRevoked());
        entity.setUsed(certificate.isUsed());

        return entity;
    }

}
