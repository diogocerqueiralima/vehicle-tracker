package com.github.diogocerqueiralima.asset.service.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic API response wrapper.
 *
 * @param message response message.
 * @param data response payload.
 * @param <T> payload type.
 */
@Schema(description = "Generic API response wrapper.")
public record ApiResponseDTO<T>(

		@Schema(description = "Human-readable response message.", example = "Resource created successfully.")
		@JsonProperty("message") String message,

		@Schema(description = "Response payload. May be null for operations that return no data.")
		@JsonProperty("data") T data

) {}