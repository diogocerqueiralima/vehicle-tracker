package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.CertificateSubject;
import com.github.diogocerqueiralima.domain.model.RegularCertificate;
import com.github.diogocerqueiralima.infrastructure.entities.CertificateEntity;
import org.springframework.stereotype.Component;

@Component
public class CertificateMapper {

    public RegularCertificate toDomain(CertificateEntity entity) {

        return new RegularCertificate(
                entity.getSerialNumber(),
                CertificateSubject.fromString(entity.getSubject()),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.isRevoked()
        );
    }

    public CertificateEntity toEntity(RegularCertificate certificate) {

        CertificateEntity entity = new CertificateEntity();

        entity.setSerialNumber(certificate.getSerialNumber());
        entity.setSubject(certificate.getSubject().toString());
        entity.setIssuedAt(certificate.getIssuedAt());
        entity.setExpiresAt(certificate.getExpiresAt());
        entity.setRevoked(certificate.isRevoked());

        return entity;
    }

}
