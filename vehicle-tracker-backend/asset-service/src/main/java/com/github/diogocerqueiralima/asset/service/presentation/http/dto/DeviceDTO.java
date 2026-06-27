package com.github.diogocerqueiralima.asset.service.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Device payload returned by presentation endpoints.
 */
@Schema(description = "Device information returned by the API.")
public record DeviceDTO(

        @Schema(description = "Unique identifier of the device.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("id") UUID id,

        @Schema(description = "Timestamp when the device was created.", example = "2024-01-15T10:30:00Z")
        @JsonProperty("created_at") Instant createdAt,

        @Schema(description = "Timestamp when the device was last updated.", example = "2024-06-01T08:00:00Z")
        @JsonProperty("updated_at") Instant updatedAt,

        @Schema(description = "Manufacturer-assigned serial number of the device.", example = "SN-00123456")
        @JsonProperty("serial_number") String serialNumber,

        @Schema(description = "Model name of the device.", example = "TrackPro X200")
        @JsonProperty("model") String model,

        @Schema(description = "Manufacturer of the device.", example = "Teltonika")
        @JsonProperty("manufacturer") String manufacturer,

        @Schema(description = "International Mobile Equipment Identity (IMEI) of the device.", example = "352099001761481")
        @JsonProperty("imei") String imei

) {}
