package com.github.diogocerqueiralima.presentation.http.dto;

public record CreateVehicleDTO(String vin, String plate, String model, String manufacturer, Integer year) {}
