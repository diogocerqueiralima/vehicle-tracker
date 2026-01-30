package com.github.diogocerqueiralima.infrastructure.exceptions;

public class InvalidCommonNameException extends RuntimeException {

    public InvalidCommonNameException() {
        super("The common name is invalid.");
    }

}
