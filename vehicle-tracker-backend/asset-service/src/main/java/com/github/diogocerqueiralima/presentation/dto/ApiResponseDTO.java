package com.github.diogocerqueiralima.presentation.dto;

/**
 * Generic API response wrapper.
 *
 * @param message response message.
 * @param data response payload.
 * @param <T> payload type.
 */
public record ApiResponseDTO<T>(String message, T data) {}
