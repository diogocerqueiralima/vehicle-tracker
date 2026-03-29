package com.github.diogocerqueiralima.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Plate validation supporting Portuguese formats.
 */
public class PlateValidator implements ConstraintValidator<Plate, String> {

    // PT plates must use 3 groups of 2 chars separated by hyphens.
    // Supported generations: 00-00-AA, 00-AA-00, AA-00-00, AA-00-AA.
    private static final Pattern PT_PLATE_PATTERN =
            Pattern.compile("^(?:[0-9]{2}-[0-9]{2}-[A-Z]{2}|[0-9]{2}-[A-Z]{2}-[0-9]{2}|[A-Z]{2}-[0-9]{2}-[0-9]{2}|[A-Z]{2}-[0-9]{2}-[A-Z]{2})$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }

        String normalizedValue = value.trim().toUpperCase();

        return PT_PLATE_PATTERN.matcher(normalizedValue).matches();
    }
}


