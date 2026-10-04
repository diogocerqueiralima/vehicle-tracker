package com.github.diogocerqueiralima.asset.service.presentation.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Request payload for creating or updating a device.")
data class CreateOrUpdateDeviceRequestDTO(
    @field:Schema(description = "Manufacturer-assigned serial number of the device.", example = "SN-00123456")
    @JsonProperty("serial_number")
    val serialNumber: String,
    @field:Schema(description = "Model name of the device.", example = "TrackPro X200")
    @JsonProperty("model")
    val model: String,
    @field:Schema(description = "Manufacturer of the device.", example = "Teltonika")
    @JsonProperty("manufacturer")
    val manufacturer: String,
    @field:Schema(description = "International Mobile Equipment Identity (IMEI) of the device.", example = "352099001761481")
    @JsonProperty("imei")
    val imei: String,
    @field:Schema(description = "Identifier of the user who owns the device.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("owner_id")
    val ownerId: UUID
)
