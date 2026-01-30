package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.exceptions.CertificateCouldNotBeParsedException;
import com.github.diogocerqueiralima.application.exceptions.CertificateCouldNotBeSignedException;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler({ CertificateCouldNotBeSignedException.class, CertificateCouldNotBeParsedException.class })
    public ResponseEntity<ApiResponseDTO<Void>> handleInternalServerError(RuntimeException e) {

        String message = e.getMessage() == null ? "An internal server error occurred." : e.getMessage();

        return ResponseEntity.internalServerError()
                .body(new ApiResponseDTO<>(message, null));
    }

}
