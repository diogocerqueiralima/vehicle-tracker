package com.github.diogocerqueiralima.infrastructure.exceptions;

public class InvalidSignatureException extends RuntimeException {

    public InvalidSignatureException() {
        super("The signature is invalid.");
    }

}
