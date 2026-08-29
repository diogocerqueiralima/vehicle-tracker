package com.github.diogocerqueiralima.domain.devices.model

import java.util.UUID

data class ScannedDevice(

    val id: UUID,
    val address: String,
    val rssi: Int

)
