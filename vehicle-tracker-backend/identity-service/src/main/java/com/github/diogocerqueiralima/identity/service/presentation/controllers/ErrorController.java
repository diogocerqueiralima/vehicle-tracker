package com.github.diogocerqueiralima.identity.service.presentation.controllers;

import com.github.diogocerqueiralima.identity.service.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.identity.service.application.exceptions.DeviceNotOwnedException;
import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException;
import com.github.diogocerqueiralima.identity.service.presentation.dto.ApiResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleValidationException(ConstraintViolationException e) {

        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("There is an error in some of the parameters of the request.");

        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(message, null));
    }

    @ExceptionHandler({DeviceNotOwnedException.class})
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(RuntimeException e) {

        String message = e.getMessage() == null ? "Bad request." : e.getMessage();

        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(message, null));
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(RuntimeException e) {

        String message = e.getMessage() == null ? "The requested resource was not found." : e.getMessage();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDTO<>(message, null));
    }

    @ExceptionHandler(OperationFailedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInternalServerError(OperationFailedException e) {

        String message = e.getMessage() == null ? "An internal server error occurred." : e.getMessage();

        return ResponseEntity.internalServerError()
                .body(new ApiResponseDTO<>(message, null));
    }

}
