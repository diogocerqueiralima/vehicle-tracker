package com.github.diogocerqueiralima.presentation.http.dto;

import java.util.UUID;

public record DeviceDTO(UUID id, String imei, String serialNumber, String manufacturer, UUID vehicleId) {}
