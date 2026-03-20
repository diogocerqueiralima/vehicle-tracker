package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized HTTP exception mapping for presentation errors.
 */
@RestControllerAdvice
public class ErrorController {

    /**
     * Handles bean validation errors thrown by request body binding.
     *
     * @param exception method argument validation exception.
     * @return bad request response containing aggregated validation messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage() == null ? "Invalid request body." : fieldError.getDefaultMessage())
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("There is an error in some of the parameters of the request.");

        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     * Handles bean validation errors thrown by validated method parameters.
     *
     * @param exception constraint violation exception.
     * @return bad request response containing aggregated validation messages.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleConstraintViolation(ConstraintViolationException exception) {

        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("There is an error in some of the parameters of the request.");

        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     * Handles bad requests by the client.
     *
     * @param exception bad request exception.
     * @return bad request response.
     */
    @ExceptionHandler(VehicleAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(VehicleAlreadyExistsException exception) {

        String message = exception.getMessage() == null ? "Bad request" : exception.getMessage();

        return ResponseEntity.status(400)
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     * Handles not-found requests.
     *
     * @param exception not found exception.
     * @return not found response.
     */
    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(VehicleNotFoundException exception) {

        String message = exception.getMessage() == null ? "Not found" : exception.getMessage();

        return ResponseEntity.status(404)
                .body(new ApiResponseDTO<>(message, null));
    }

}

