package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.CertificateInfo;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateInfoPersistence;
import com.github.diogocerqueiralima.infrastructure.entities.CertificateInfoEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.CertificateInfoMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.CertificateInfoRepository;
import org.springframework.stereotype.Component;

@Component
public class CertificateInfoPersistenceImpl implements CertificateInfoPersistence {

    private final CertificateInfoMapper certificateInfoMapper;
    private final CertificateInfoRepository certificateInfoRepository;

    public CertificateInfoPersistenceImpl(
            CertificateInfoMapper certificateInfoMapper, CertificateInfoRepository certificateInfoRepository
    ) {
        this.certificateInfoMapper = certificateInfoMapper;
        this.certificateInfoRepository = certificateInfoRepository;
    }

    @Override
    public CertificateInfo save(CertificateInfo certificateInfo) {

        CertificateInfoEntity entity = certificateInfoMapper.toEntity(certificateInfo);
        CertificateInfoEntity savedEntity = certificateInfoRepository.save(entity);

        return certificateInfoMapper.toDomain(savedEntity);
    }

}
