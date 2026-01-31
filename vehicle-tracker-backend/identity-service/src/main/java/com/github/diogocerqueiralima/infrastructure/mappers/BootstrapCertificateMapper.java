package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;
import com.github.diogocerqueiralima.domain.model.CertificateInfo;
import com.github.diogocerqueiralima.infrastructure.entities.BootstrapCertificateEntity;
import org.springframework.stereotype.Component;

@Component
public class BootstrapCertificateMapper {


    public BootstrapCertificate toDomain(BootstrapCertificateEntity entity) {

        CertificateInfo info = new CertificateInfo(
                entity.getSerialNumber(),
                entity.getSubject(),
                entity.getIssuedAt(),
                entity.getExpiresAt()
        );

        return new BootstrapCertificate(info, entity.isRevoked(), entity.isUsed());
    }

    public BootstrapCertificateEntity toEntity(BootstrapCertificate certificate) {

        CertificateInfo info = certificate.getInfo();
        BootstrapCertificateEntity entity = new BootstrapCertificateEntity();

        entity.setSerialNumber(info.getSerialNumber());
        entity.setSubject(info.getSubject());
        entity.setIssuedAt(info.getIssuedAt());
        entity.setExpiresAt(info.getExpiresAt());
        entity.setRevoked(certificate.isRevoked());
        entity.setUsed(certificate.isUsed());

        return entity;
    }

}
