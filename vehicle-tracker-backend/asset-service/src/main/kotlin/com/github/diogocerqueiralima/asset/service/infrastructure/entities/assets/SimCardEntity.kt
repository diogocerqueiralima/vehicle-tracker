package com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "sim_cards")
@PrimaryKeyJoinColumn(name = "id")
class SimCardEntity : AssetEntity() {

    @Column(name = "iccid", nullable = false, unique = true, length = 32)
    lateinit var iccid: String

    @Column(name = "msisdn", nullable = false, unique = true, length = 32)
    lateinit var msisdn: String

    @Column(name = "imsi", nullable = false, unique = true, length = 32)
    lateinit var imsi: String

}
