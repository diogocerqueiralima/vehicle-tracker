package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.certificate.CertificateSubject;
import com.github.diogocerqueiralima.domain.model.certificate.RegularCertificate;
import com.github.diogocerqueiralima.infrastructure.entities.CertificateEntity;
import org.springframework.stereotype.Component;

@Component
public class CertificateMapper {

    /**
     *
     * Converts a {@link CertificateEntity} to a {@link RegularCertificate} domain object.
     *
     * @param entity the {@link CertificateEntity} to be converted
     * @return a {@link RegularCertificate} domain object corresponding to the provided entity
     */
    public RegularCertificate toDomain(CertificateEntity entity) {

        return new RegularCertificate(
                entity.getSerialNumber(),
                CertificateSubject.fromString(entity.getSubject()),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.isRevoked()
        );
    }

    /**
     *
     * Converts a {@link RegularCertificate} domain object to a {@link CertificateEntity}.
     *
     * @param certificate the {@link RegularCertificate} to be converted
     * @return a {@link CertificateEntity} corresponding to the provided domain object
     */
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
