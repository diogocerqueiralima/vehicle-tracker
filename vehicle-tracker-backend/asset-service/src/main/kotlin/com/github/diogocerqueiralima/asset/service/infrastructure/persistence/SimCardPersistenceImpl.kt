package com.github.diogocerqueiralima.asset.service.infrastructure.persistence

import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardPersistence
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.SimCardMapper
import com.github.diogocerqueiralima.asset.service.infrastructure.repositories.SimCardRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SimCardPersistenceImpl(private val simCardRepository: SimCardRepository) : SimCardPersistence {

    override fun save(simCard: SimCard): SimCard {

        val entity = SimCardMapper.toEntity(simCard)
        val savedEntity = simCardRepository.save(entity)

        return SimCardMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): SimCard? =
        simCardRepository.findByIdOrNull(id)?.let(SimCardMapper::toDomain)

    override fun findByIdAndOwnerId(id: UUID, ownerId: UUID): SimCard? =
        simCardRepository.findByIdAndOwnerId(id, ownerId)?.let(SimCardMapper::toDomain)

    override fun deleteByIdAndOwnerId(id: UUID, ownerId: UUID) {
        simCardRepository.deleteByIdAndOwnerId(id, ownerId)
    }

    override fun existsByIccidOrMsisdnOrImsi(iccid: String, msisdn: String, imsi: String): Boolean =
        simCardRepository.existsByIccidOrMsisdnOrImsi(iccid, msisdn, imsi)

    override fun isIccidOrMsisdnOrImsiTakenByAnotherSimCard(
        iccid: String, msisdn: String, imsi: String, excludingId: UUID
    ): Boolean =
        simCardRepository.existsByIccidOrMsisdnOrImsiAndIdNot(iccid, msisdn, imsi, excludingId)

}
