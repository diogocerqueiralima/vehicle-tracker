package com.github.diogocerqueiralima.application.validators;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlateValidatorTest {

    private final Plate.PlateValidator validator = new Plate.PlateValidator();

    @Test
    public void plate_without_hyphens_should_fail() {
        assertFalse(validator.isValid("A121FD3", null));
    }

    @Test
    public void plate_with_invalid_character_count_between_hyphens_should_fail() {
        assertFalse(validator.isValid("ABC-1-1A", null));
    }

    @Test
    public void plate_with_special_characters_should_fail() {
        assertFalse(validator.isValid("AB-@1-C3", null));
    }

    @Test
    public void plate_with_lowercase_letters_should_fail() {
        assertFalse(validator.isValid("ab-12-c3", null));
    }

    @Test
    public void plate_with_missing_sections_should_fail() {
        assertFalse(validator.isValid("AB-12", null));
    }

    @Test
    public void plate_with_extra_sections_should_fail() {
        assertFalse(validator.isValid("AB-12-C3-D4", null));
    }

    @Test
    public void plate_with_valid_format_should_pass() {
        assertTrue(validator.isValid("AB-12-C3", null));
    }

    @Test
    public void null_plate_should_pass() {
        assertTrue(validator.isValid(null, null));
    }

}
