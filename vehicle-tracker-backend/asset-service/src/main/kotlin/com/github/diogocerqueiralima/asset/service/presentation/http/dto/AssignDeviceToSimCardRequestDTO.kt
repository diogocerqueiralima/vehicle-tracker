package com.github.diogocerqueiralima.asset.service.presentation.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Request payload for assigning a device to a SIM card.")
data class AssignDeviceToSimCardRequestDTO(
    @field:Schema(description = "Unique identifier of the device to assign.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("device_id")
    val deviceId: UUID
)
