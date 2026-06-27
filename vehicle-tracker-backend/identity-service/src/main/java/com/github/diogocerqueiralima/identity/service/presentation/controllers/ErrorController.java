package com.github.diogocerqueiralima.identity.service.presentation.controllers;

import com.github.diogocerqueiralima.error.common.exceptions.ForbiddenException;
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException;
import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException;
import com.github.diogocerqueiralima.identity.service.presentation.dto.ApiResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized HTTP exception mapping for the identity service.
 */
@RestControllerAdvice
public class ErrorController {

    /**
     * Handles bean validation errors thrown by validated method parameters.
     *
     * @param e constraint violation exception.
     * @return bad request response containing aggregated validation messages.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleValidationException(ConstraintViolationException e) {

        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("There is an error in some of the parameters of the request.");

        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     * Handles authorization failures such as ownership violations.
     *
     * @param e forbidden exception.
     * @return forbidden response.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleForbidden(ForbiddenException e) {

        String message = e.getMessage() == null ? "Forbidden." : e.getMessage();

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     * Handles not-found requests.
     *
     * @param e not found exception.
     * @return not found response.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(NotFoundException e) {

        String message = e.getMessage() == null ? "The requested resource was not found." : e.getMessage();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     * Handles operation failures caused by infrastructure or domain errors.
     *
     * @param e operation failed exception.
     * @return internal server error response.
     */
    @ExceptionHandler(OperationFailedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInternalServerError(OperationFailedException e) {

        String message = e.getMessage() == null ? "An unexpected error has occurred." : e.getMessage();

        return ResponseEntity.internalServerError()
                .body(new ApiResponseDTO<>(message, null));
    }

}
