package com.github.diogocerqueiralima.asset.service.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Request DTO for vehicle creation.
 */
@Schema(description = "Request payload for creating a new vehicle.")
public record CreateVehicleRequestDTO(

        @Schema(description = "Vehicle Identification Number (VIN).", example = "1HGCM82633A123456")
        @JsonProperty("vin")
        String vin,

        @Schema(description = "License plate number of the vehicle.", example = "AB-12-CD")
        @JsonProperty("plate")
        String plate,

        @Schema(description = "Model name of the vehicle.", example = "Civic")
        @JsonProperty("model")
        String model,

        @Schema(description = "Manufacturer of the vehicle.", example = "Honda")
        @JsonProperty("manufacturer")
        String manufacturer,

        @Schema(description = "Date when the vehicle was manufactured.", example = "2020-03-15")
        @JsonProperty("manufacturing_date")
        LocalDate manufacturingDate

) {}
