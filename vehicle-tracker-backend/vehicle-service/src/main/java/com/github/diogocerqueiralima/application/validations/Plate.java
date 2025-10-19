package com.github.diogocerqueiralima.application.validations;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 *
 * The Plate annotation is used to validate Portuguese vehicle plate numbers.
 * The expected format is "AA-00-AA", where 'A' represents an uppercase letter
 * and '0' represents a digit.
 * <p>
 * If the field is null, it is considered valid.
 *
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Plate.PlateValidator.class)
public @interface Plate {

    String message() default "Invalid Portuguese vehicle plate";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class PlateValidator implements ConstraintValidator<Plate, String> {

        private static final String PLATE_REGEX = "^[A-Z0-9]{2}-[A-Z0-9]{2}-[A-Z0-9]{2}$";

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {

            if (value == null) {
                return true;
            }

            return value.matches(PLATE_REGEX);
        }

    }

}