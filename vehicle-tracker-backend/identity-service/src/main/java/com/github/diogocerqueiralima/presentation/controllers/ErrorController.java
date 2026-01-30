package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.infrastructure.exceptions.CertificateCouldNotBeParsedException;
import com.github.diogocerqueiralima.infrastructure.exceptions.*;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler({
            InvalidSignatureException.class, InvalidCertificateSigningRequestException.class,
            InvalidCommonNameException.class, InvalidCertificateException.class,
            InvalidCertificateException.class
    })
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(RuntimeException e) {

        String message = e.getMessage() == null ? "Bad request." : e.getMessage();

        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(message, null));
    }

    @ExceptionHandler({
            CouldNotConvertCertificateToPEMException.class, SignatureVerificationException.class,
            CouldNotSignCertificateException.class, CertificateCouldNotBeParsedException.class
    })
    public ResponseEntity<ApiResponseDTO<Void>> handleInternalServerError(RuntimeException e) {

        String message = e.getMessage() == null ? "An internal server error occurred." : e.getMessage();

        return ResponseEntity.internalServerError()
                .body(new ApiResponseDTO<>(message, null));
    }

}
