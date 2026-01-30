package com.github.diogocerqueiralima.presentation.dto;

public record ApiResponseDTO<T>(String message, T data) {}
