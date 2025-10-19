package com.github.diogocerqueiralima.application.validations;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 *
 * The Year annotation is a custom validation constraint used to validate that an integer field
 * represents a valid year. The valid range is from 1886 (the year the first automobile was invented)
 * to the current year.
 * <p>
 * If the field is null, it is considered valid.
 *
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Year.YearValidator.class)
public @interface Year {

    String message() default "the year must be between 1886 and the current year";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class YearValidator implements ConstraintValidator<Year, Integer> {

        private static final int MIN_YEAR = 1886;

        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {

            if (value == null) {
                return true;
            }

            int currentYear = java.time.Year.now().getValue();
            return value >= MIN_YEAR && value <= currentYear;
        }

    }

}
