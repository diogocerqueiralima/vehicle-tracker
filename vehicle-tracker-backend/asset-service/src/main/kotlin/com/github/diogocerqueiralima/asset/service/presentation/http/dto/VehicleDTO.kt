package com.github.diogocerqueiralima.asset.service.presentation.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Schema(description = "Vehicle information returned by the API.")
data class VehicleDTO(
    @field:Schema(description = "Unique identifier of the vehicle.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("id")
    val id: UUID,
    @field:Schema(description = "Timestamp when the vehicle was created.", example = "2024-01-15T10:30:00Z")
    @JsonProperty("created_at")
    val createdAt: Instant,
    @field:Schema(description = "Timestamp when the vehicle was last updated.", example = "2024-06-01T08:00:00Z")
    @JsonProperty("updated_at")
    val updatedAt: Instant,
    @field:Schema(description = "Vehicle Identification Number (VIN).", example = "1HGCM82633A123456")
    @JsonProperty("vin")
    val vin: String,
    @field:Schema(description = "License plate number of the vehicle.", example = "AB-12-CD")
    @JsonProperty("plate")
    val plate: String,
    @field:Schema(description = "Model name of the vehicle.", example = "Civic")
    @JsonProperty("model")
    val model: String,
    @field:Schema(description = "Manufacturer of the vehicle.", example = "Honda")
    @JsonProperty("manufacturer")
    val manufacturer: String,
    @field:Schema(description = "Date when the vehicle was manufactured.", example = "2020-03-15")
    @JsonProperty("manufacturing_date")
    val manufacturingDate: LocalDate
)
