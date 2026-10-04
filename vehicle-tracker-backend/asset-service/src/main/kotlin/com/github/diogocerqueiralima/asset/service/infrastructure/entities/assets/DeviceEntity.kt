package com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "devices")
@PrimaryKeyJoinColumn(name = "id")
class DeviceEntity : AssetEntity() {

    @Column(name = "serial_number", nullable = false, unique = true)
    lateinit var serialNumber: String

    @Column(name = "model", nullable = false)
    lateinit var model: String

    @Column(name = "manufacturer", nullable = false)
    lateinit var manufacturer: String

    @Column(name = "imei", nullable = false, unique = true)
    lateinit var imei: String

}
