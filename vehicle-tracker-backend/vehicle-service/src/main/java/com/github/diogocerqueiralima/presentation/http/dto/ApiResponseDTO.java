package com.github.diogocerqueiralima.presentation.http.dto;

public record ApiResponseDTO<T>(T data, String message) {

    public static <T> ApiResponseDTO<T> of(T data, String message) {
        return new ApiResponseDTO<>(data, message);
    }

}
