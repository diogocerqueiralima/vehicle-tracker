package com.github.diogocerqueiralima.asset.service.presentation.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Device information returned by the API.")
data class DeviceDTO(
    @field:Schema(description = "Unique identifier of the device.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("id")
    val id: UUID,
    @field:Schema(description = "Timestamp when the device was created.", example = "2024-01-15T10:30:00Z")
    @JsonProperty("created_at")
    val createdAt: Instant,
    @field:Schema(description = "Timestamp when the device was last updated.", example = "2024-06-01T08:00:00Z")
    @JsonProperty("updated_at")
    val updatedAt: Instant,
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
    val imei: String
)
