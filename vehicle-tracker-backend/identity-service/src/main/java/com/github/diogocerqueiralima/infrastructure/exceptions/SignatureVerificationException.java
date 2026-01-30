package com.github.diogocerqueiralima.infrastructure.exceptions;

public class SignatureVerificationException extends RuntimeException {

    public SignatureVerificationException() {
        super("The signature could not be verified.");
    }

}
