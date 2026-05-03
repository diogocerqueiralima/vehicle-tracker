package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.SimCard;
import com.github.diogocerqueiralima.application.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.infrastructure.entities.SimCardEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.SimCardMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.SimCardRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    public Optional<SimCard> findByIccid(String iccid) {
        return simCardRepository
                .findById(iccid)
                .map(SimCardMapper::toDomain);
    }

    @Override
    public boolean existsByIccid(String iccid) {
        return simCardRepository.existsById(iccid);
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
    public void deleteByIccid(String iccid) {
        simCardRepository.deleteById(iccid);
    }

}

