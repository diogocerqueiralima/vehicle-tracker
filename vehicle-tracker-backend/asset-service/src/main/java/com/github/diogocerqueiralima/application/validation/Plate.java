package com.github.diogocerqueiralima.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates a Portuguese license plate.
 */
@Documented
@Constraint(validatedBy = PlateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Plate {

    String message() default "plate is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


