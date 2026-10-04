package com.github.diogocerqueiralima.asset.service.infrastructure.repositories

import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.SimCardEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SimCardRepository : JpaRepository<SimCardEntity, UUID> {

    fun findByIdAndOwnerId(id: UUID, ownerId: UUID): SimCardEntity?

    fun deleteByIdAndOwnerId(id: UUID, ownerId: UUID)

    fun existsByIccidOrMsisdnOrImsi(iccid: String, msisdn: String, imsi: String): Boolean

    fun existsByIccidOrMsisdnOrImsiAndIdNot(iccid: String, msisdn: String, imsi: String, id: UUID): Boolean

}
