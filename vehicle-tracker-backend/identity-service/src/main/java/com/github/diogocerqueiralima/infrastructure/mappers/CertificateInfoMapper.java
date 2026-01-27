package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.CertificateInfo;
import com.github.diogocerqueiralima.infrastructure.entities.CertificateInfoEntity;
import org.springframework.stereotype.Component;

@Component
public class CertificateInfoMapper {

    public CertificateInfo toDomain(CertificateInfoEntity entity) {
        return new CertificateInfo(
                entity.getSerialNumber(),
                entity.getSubject(),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.isRevoked()
        );
    }

    public CertificateInfoEntity toEntity(CertificateInfo certificateInfo) {

        CertificateInfoEntity entity = new CertificateInfoEntity();
        entity.setSerialNumber(certificateInfo.getSerialNumber());
        entity.setSubject(certificateInfo.getSubject());
        entity.setIssuedAt(certificateInfo.getIssuedAt());
        entity.setExpiresAt(certificateInfo.getExpiresAt());
        entity.setRevoked(certificateInfo.isRevoked());

        return entity;
    }

}
