package com.github.diogocerqueiralima.asset.service.presentation.http.controllers;

import com.github.diogocerqueiralima.asset.service.presentation.http.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.error.common.exceptions.ConflictException;
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException;
import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
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
     * Handles internal server errors when an operation fails unexpectedly.
     *
     * @param exception operation failed exception.
     * @return bad request response.
     */
    @ExceptionHandler(OperationFailedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInternalServerError(OperationFailedException exception) {

        String message = exception.getMessage() == null ? "An unexpected error has occurred" : exception.getMessage();

        return ResponseEntity.internalServerError()
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     * Handles conflict errors when trying to create resources that already exist.
     *
     * @param exception conflict exception.
     * @return conflict response.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleConflict(ConflictException exception) {

        String message = exception.getMessage() == null ? "Conflict" : exception.getMessage();

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     * Handles not-found requests.
     *
     * @param exception not found exception.
     * @return not found response.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(NotFoundException exception) {

        String message = exception.getMessage() == null ? "Not found" : exception.getMessage();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDTO<>(message, null));
    }

}

