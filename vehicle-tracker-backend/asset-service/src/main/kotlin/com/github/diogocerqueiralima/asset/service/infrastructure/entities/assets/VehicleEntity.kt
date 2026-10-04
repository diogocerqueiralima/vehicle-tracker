package com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "vehicles")
@PrimaryKeyJoinColumn(name = "id")
class VehicleEntity : AssetEntity() {

    @Column(name = "vin", nullable = false, unique = true)
    lateinit var vin: String

    @Column(name = "plate", nullable = false, unique = true)
    lateinit var plate: String

    @Column(name = "model", nullable = false)
    lateinit var model: String

    @Column(name = "manufacturer", nullable = false)
    lateinit var manufacturer: String

    @Column(name = "manufacturing_date", nullable = false)
    lateinit var manufacturingDate: LocalDate

}
