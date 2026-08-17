package com.github.diogocerqueiralima.asset.service.infrastructure.persistence;

import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.SimCardEntity;
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.SimCardMapper;
import com.github.diogocerqueiralima.asset.service.infrastructure.repositories.SimCardRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.github.diogocerqueiralima.asset.service.infrastructure.mappers.SimCardMapper.toDomain;
import static com.github.diogocerqueiralima.asset.service.infrastructure.mappers.SimCardMapper.toEntity;

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
    public void deleteByIdAndOwnerId(UUID id, UUID ownerId) {
        simCardRepository.deleteByIdAndOwnerId(id, ownerId);
    }

    @Override
    public boolean existsByIccidOrMsisdnOrImsi(String iccid, String msisdn, String imsi) {
        return simCardRepository.existsByIccidOrMsisdnOrImsi(iccid, msisdn, imsi);
    }

    @Override
    public boolean isIccidOrMsisdnOrImsiTakenByAnotherSimCard(String iccid, String msisdn, String imsi, UUID excludingId) {
        return simCardRepository.existsByIccidOrMsisdnOrImsiAndIdNot(iccid, msisdn, imsi, excludingId);
    }

}

