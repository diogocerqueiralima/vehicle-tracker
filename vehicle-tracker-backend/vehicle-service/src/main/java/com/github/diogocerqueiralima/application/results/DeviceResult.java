package com.github.diogocerqueiralima.application.results;

import java.util.UUID;

public record DeviceResult(UUID id, String imei, String serialNumber, String manufacturer, UUID vehicleId) {}
