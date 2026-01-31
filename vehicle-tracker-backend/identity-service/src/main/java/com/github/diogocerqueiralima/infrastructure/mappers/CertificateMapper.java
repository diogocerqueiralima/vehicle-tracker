package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.model.CertificateInfo;
import com.github.diogocerqueiralima.infrastructure.entities.CertificateEntity;
import org.springframework.stereotype.Component;

@Component
public class CertificateMapper {


    public Certificate toDomain(CertificateEntity entity) {

        CertificateInfo info = new CertificateInfo(
                entity.getSerialNumber(),
                entity.getSubject(),
                entity.getIssuedAt(),
                entity.getExpiresAt()
        );

        return new Certificate(info, entity.isRevoked());
    }

    public CertificateEntity toEntity(Certificate certificate) {

        CertificateInfo info = certificate.getInfo();
        CertificateEntity entity = new CertificateEntity();

        entity.setSerialNumber(info.getSerialNumber());
        entity.setSubject(info.getSubject());
        entity.setIssuedAt(info.getIssuedAt());
        entity.setExpiresAt(info.getExpiresAt());
        entity.setRevoked(certificate.isRevoked());

        return entity;
    }

}
