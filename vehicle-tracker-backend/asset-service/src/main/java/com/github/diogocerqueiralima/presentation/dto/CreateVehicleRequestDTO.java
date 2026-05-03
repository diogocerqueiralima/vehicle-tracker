package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Request DTO for vehicle creation.
 */
public record CreateVehicleRequestDTO(

        @JsonProperty("vin")
        String vin,

        @JsonProperty("plate")
        String plate,

        @JsonProperty("model")
        String model,

        @JsonProperty("manufacturer")
        String manufacturer,

        @JsonProperty("manufacturing_date")
        LocalDate manufacturingDate

) {}
