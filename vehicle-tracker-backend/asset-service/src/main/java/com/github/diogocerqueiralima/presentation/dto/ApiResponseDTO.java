package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic API response wrapper.
 *
 * @param message response message.
 * @param data response payload.
 * @param <T> payload type.
 */
public record ApiResponseDTO<T>(
		@JsonProperty("message") String message,
		@JsonProperty("data") T data
) {}
