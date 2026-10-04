package com.github.diogocerqueiralima.api.common.controllers

import com.github.diogocerqueiralima.api.common.dto.ApiResponseDTO
import com.github.diogocerqueiralima.error.common.exceptions.BadRequestException
import com.github.diogocerqueiralima.error.common.exceptions.BaseException
import com.github.diogocerqueiralima.error.common.exceptions.ConflictException
import com.github.diogocerqueiralima.error.common.exceptions.ForbiddenException
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * This class is a global exception handler for REST controllers in the application.
 * It provides a centralized way to handle exceptions and return appropriate HTTP responses.
 */
@RestControllerAdvice
class ErrorBaseController {

    /**
     * Handles [BadRequestException] and returns a ResponseEntity with a 400 Bad Request status.
     *
     * @param e The [BadRequestException] that was thrown.
     * @return A ResponseEntity containing an [ApiResponseDTO] with the error message and a null data payload.
     */
    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(e: BadRequestException): ResponseEntity<ApiResponseDTO<Void?>> =
        error(HttpStatus.BAD_REQUEST, e.message ?: "Bad Request")

    /**
     * Handles [ForbiddenException] and returns a ResponseEntity with a 403 Forbidden status.
     *
     * @param e The [ForbiddenException] that was thrown.
     * @return A ResponseEntity containing an [ApiResponseDTO] with the error message and a null data payload.
     */
    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(e: ForbiddenException): ResponseEntity<ApiResponseDTO<Void?>> =
        error(HttpStatus.FORBIDDEN, e.message ?: "Forbidden")

    /**
     * Handles [NotFoundException] and returns a ResponseEntity with a 404 Not Found status.
     *
     * @param e The [NotFoundException] that was thrown.
     * @return A ResponseEntity containing an [ApiResponseDTO] with the error message and a null data payload.
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ResponseEntity<ApiResponseDTO<Void?>> =
        error(HttpStatus.NOT_FOUND, e.message ?: "Not Found")

    /**
     * Handles [ConflictException] and returns a ResponseEntity with a 409 Conflict status.
     *
     * @param e The [ConflictException] that was thrown.
     * @return A ResponseEntity containing an [ApiResponseDTO] with the error message and a null data payload.
     */
    @ExceptionHandler(ConflictException::class)
    fun handleConflict(e: ConflictException): ResponseEntity<ApiResponseDTO<Void?>> =
        error(HttpStatus.CONFLICT, e.message ?: "Conflict")

    /**
     * Handles [OperationFailedException] and returns a ResponseEntity with a 500 Internal Server Error status.
     *
     * @param e The [OperationFailedException] that was thrown.
     * @return A ResponseEntity containing an [ApiResponseDTO] with the error message and a null data payload.
     */
    @ExceptionHandler(OperationFailedException::class)
    fun handleOperationFailed(e: OperationFailedException): ResponseEntity<ApiResponseDTO<Void?>> =
        error(HttpStatus.INTERNAL_SERVER_ERROR, e.message ?: "Operation Failed")

    /**
     * Handles [BaseException] and returns a ResponseEntity with a 500 Internal Server Error status.
     *
     * @param e The [BaseException] that was thrown.
     * @return A ResponseEntity containing an [ApiResponseDTO] with the error message and a null data payload.
     */
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(e: BaseException): ResponseEntity<ApiResponseDTO<Void?>> =
        error(HttpStatus.INTERNAL_SERVER_ERROR, e.message ?: "An error occurred")

    /**
     * Handles bean validation errors thrown by validated method parameters.
     *
     * @param e constraint violation exception.
     * @return bad request response containing aggregated validation messages.
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationException(e: ConstraintViolationException): ResponseEntity<ApiResponseDTO<Void?>> {

        val message = e.constraintViolations
            .map { it.message }
            .reduceOrNull { s1, s2 -> "$s1; $s2" }
            ?: "There is an error in some of the parameters of the request."

        return error(HttpStatus.BAD_REQUEST, message)
    }

    /**
     * Handles bean validation errors thrown by request body binding.
     *
     * @param exception method argument validation exception.
     * @return bad request response containing aggregated validation messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(exception: MethodArgumentNotValidException): ResponseEntity<ApiResponseDTO<Void?>> {

        val message = exception.bindingResult.fieldErrors
            .map { it.defaultMessage ?: "Invalid request body." }
            .reduceOrNull { s1, s2 -> "$s1; $s2" }
            ?: "There is an error in some of the parameters of the request."

        return error(HttpStatus.BAD_REQUEST, message)
    }

    /**
     * Builds the error response shared by every handler.
     *
     * @param status HTTP status of the response.
     * @param message error message returned to the client.
     * @return a ResponseEntity containing an [ApiResponseDTO] with the message and a null data payload.
     */
    private fun error(status: HttpStatus, message: String): ResponseEntity<ApiResponseDTO<Void?>> =
        ResponseEntity.status(status).body(ApiResponseDTO(message, null))

}
