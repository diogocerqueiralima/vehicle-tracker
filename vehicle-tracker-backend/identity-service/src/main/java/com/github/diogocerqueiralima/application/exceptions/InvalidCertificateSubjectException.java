package com.github.diogocerqueiralima.application.exceptions;

/**
 * Exception thrown when a certificate subject is invalid.
 */
public class InvalidCertificateSubjectException extends RuntimeException {

    /**
     *
     * Constructs a new {@link InvalidCertificateSubjectException} with the specified detail message.
     *
     * @param message the detail message explaining why the certificate subject is invalid
     */
    public InvalidCertificateSubjectException(String message) {
        super(message);
    }

}
