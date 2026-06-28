package com.github.diogocerqueiralima.api.common.controllers;

import com.github.diogocerqueiralima.api.common.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.error.common.exceptions.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * This class is a global exception handler for REST controllers in the application.
 * It provides a centralized way to handle exceptions and return appropriate HTTP responses.
 */
@RestControllerAdvice
public class ErrorBaseController {

    /**
     *
     * Handles {@link BadRequestException} and returns a ResponseEntity with a 400 Bad Request status.
     *
     * @param e The {@link BadRequestException} that was thrown.
     * @return A ResponseEntity containing an {@link ApiResponseDTO} with the error message and a null data payload.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(BadRequestException e) {

        String message = e.getMessage() == null ? "Bad Request" : e.getMessage();

        return ResponseEntity
                .badRequest()
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     *
     * Handles {@link ForbiddenException} and returns a ResponseEntity with a 403 Forbidden status.
     *
     * @param e The {@link ForbiddenException} that was thrown.
     * @return A ResponseEntity containing an {@link ApiResponseDTO} with the error message and a null data payload.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleForbidden(ForbiddenException e) {

        String message = e.getMessage() == null ? "Forbidden" : e.getMessage();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     *
     * Handles {@link NotFoundException} and returns a ResponseEntity with a 404 Not Found status.
     *
     * @param e The {@link NotFoundException} that was thrown.
     * @return A ResponseEntity containing an {@link ApiResponseDTO} with the error message and a null data payload.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(NotFoundException e) {

        String message = e.getMessage() == null ? "Not Found" : e.getMessage();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     *
     * Handles {@link ConflictException} and returns a ResponseEntity with a 409 Conflict status.
     *
     * @param e The {@link ConflictException} that was thrown.
     * @return A ResponseEntity containing an {@link ApiResponseDTO} with the error message and a null data payload.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleConflict(ConflictException e) {

        String message = e.getMessage() == null ? "Conflict" : e.getMessage();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     *
     * Handles {@link OperationFailedException} and returns a ResponseEntity with a 500 Internal Server Error status.
     *
     * @param e The {@link OperationFailedException} that was thrown.
     * @return A ResponseEntity containing an {@link ApiResponseDTO} with the error message and a null data payload.
     */
    @ExceptionHandler(OperationFailedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleOperationFailed(OperationFailedException e) {

        String message = e.getMessage() == null ? "Operation Failed" : e.getMessage();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     *
     * Handles {@link BaseException} and returns a ResponseEntity with a 500 Internal Server Error status.
     *
     * @param e The {@link BaseException} that was thrown.
     * @return A ResponseEntity containing an {@link ApiResponseDTO} with the error message and a null data payload.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBaseException(BaseException e) {

        String message = e.getMessage() == null ? "An error occurred" : e.getMessage();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(message, null));
    }

    /**
     *
     * Catch-all handler for any unexpected exception not covered by more specific handlers.
     * This ensures that third-party exceptions (e.g. JPA, JDBC drivers) always produce a
     * consistent 500 response using {@link ApiResponseDTO} instead of leaking framework details.
     *
     * @param e The unhandled {@link Exception} that was thrown.
     * @return A ResponseEntity containing an {@link ApiResponseDTO} with a generic error message and a null data payload.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUnexpectedException(Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>("An unexpected error occurred", null));
    }

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

}
