package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.model.CertificateSubject;
import com.github.diogocerqueiralima.infrastructure.entities.CertificateEntity;
import org.springframework.stereotype.Component;

@Component
public class CertificateMapper {


    public Certificate toDomain(CertificateEntity entity) {

        return new Certificate(
                entity.getSerialNumber(),
                CertificateSubject.fromString(entity.getSubject()),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.isRevoked()
        );
    }

    public CertificateEntity toEntity(Certificate certificate) {

        CertificateEntity entity = new CertificateEntity();

        entity.setSerialNumber(entity.getSerialNumber());
        entity.setSubject(entity.getSubject());
        entity.setIssuedAt(entity.getIssuedAt());
        entity.setExpiresAt(entity.getExpiresAt());
        entity.setRevoked(certificate.isRevoked());

        return entity;
    }

}
