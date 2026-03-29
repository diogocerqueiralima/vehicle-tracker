package com.github.diogocerqueiralima.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * VIN validation using the standard 17-character format.
 */
public class VinValidator implements ConstraintValidator<VIN, String> {

    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }

        return VIN_PATTERN.matcher(value.trim().toUpperCase()).matches();
    }
}

