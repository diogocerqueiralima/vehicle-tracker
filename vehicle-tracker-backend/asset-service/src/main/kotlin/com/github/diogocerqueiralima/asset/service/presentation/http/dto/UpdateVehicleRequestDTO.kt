package com.github.diogocerqueiralima.asset.service.presentation.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Request payload for updating an existing vehicle.")
data class UpdateVehicleRequestDTO(
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
