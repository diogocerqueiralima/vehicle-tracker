package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.assets.SimCard;
import com.github.diogocerqueiralima.application.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.infrastructure.entities.assets.SimCardEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.SimCardMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.SimCardRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.github.diogocerqueiralima.infrastructure.mappers.SimCardMapper.toDomain;
import static com.github.diogocerqueiralima.infrastructure.mappers.SimCardMapper.toEntity;

@Service
public class SimCardPersistenceImpl implements SimCardPersistence {

    private final SimCardRepository simCardRepository;

    public SimCardPersistenceImpl(SimCardRepository simCardRepository) {
        this.simCardRepository = simCardRepository;
    }

    @Override
    public SimCard save(SimCard simCard) {

        SimCardEntity entity = toEntity(simCard);
        SimCardEntity savedEntity = simCardRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<SimCard> findById(UUID id) {
        return simCardRepository.findById(id)
                .map(SimCardMapper::toDomain);
    }

    @Override
    public Optional<SimCard> findByIdAndOwnerId(UUID id, UUID ownerId) {
        return simCardRepository.findByIdAndOwnerId(id, ownerId)
                .map(SimCardMapper::toDomain);
    }

    @Override
    public boolean existsByIccid(String iccid) {
        return simCardRepository.existsByIccid(iccid);
    }

    @Override
    public boolean existsByMsisdn(String msisdn) {
        return simCardRepository.existsByMsisdn(msisdn);
    }

    @Override
    public boolean existsByImsi(String imsi) {
        return simCardRepository.existsByImsi(imsi);
    }

    @Override
    public void deleteByIdAndOwnerId(UUID id, UUID ownerId) {
        simCardRepository.deleteByIdAndOwnerId(id, ownerId);
    }

}

