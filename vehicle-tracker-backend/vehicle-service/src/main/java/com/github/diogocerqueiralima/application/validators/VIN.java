package com.github.diogocerqueiralima.application.validators;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 *
 * The VIN annotation is used to validate Vehicle Identification Numbers (VINs).
 * The expected format is a 17-character string consisting of uppercase letters (excluding I, O, and Q)
 * and digits.
 * <p>
 * If the field is null, it is considered valid.
 *
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VIN.VINValidator.class)
public @interface VIN {

    String message() default "Invalid Portuguese vehicle plate";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class VINValidator implements ConstraintValidator<VIN, String> {

        private static final String VIN_REGEX = "\\b[(A-H|J-NPR-Z0-9)]{17}\\b";

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {

            if (value == null) {
                return true;
            }

            return value.matches(VIN_REGEX);
        }

    }

}
