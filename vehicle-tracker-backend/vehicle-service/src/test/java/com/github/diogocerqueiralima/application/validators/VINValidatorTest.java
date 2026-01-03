package com.github.diogocerqueiralima.application.validators;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VINValidatorTest {

    private final VIN.VINValidator validator = new VIN.VINValidator();

    @Test
    public void null_VIN_should_pass() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    public void VIN_with_less_than_17_characters_should_fail() {
        assertFalse(validator.isValid("1HGCM82633A00435", null));
    }

    @Test
    public void VIN_with_more_than_17_characters_should_fail() {
        assertFalse(validator.isValid("1HGCM82633A0043521", null));
    }

    @Test
    public void VIN_with_invalid_letters_should_fail() {
        assertFalse(validator.isValid("1HGCM82633A0I4352", null));
        assertFalse(validator.isValid("1HGCM82633A0O4352", null));
        assertFalse(validator.isValid("1HGCM82633A0Q4352", null));
    }

    @Test
    public void VIN_with_special_characters_should_fail() {
        assertFalse(validator.isValid("1HGCM82633A0@4352", null));
        assertFalse(validator.isValid("1HGCM82633A0#4352", null));
    }

    @Test
    public void valid_VIN_should_pass() {
        assertTrue(validator.isValid("1HGCM82633A004352", null));
        assertTrue(validator.isValid("JHMFA16586S000123", null));
    }

    @Test
    public void VIN_with_lowercase_letters_should_fail() {
        assertFalse(validator.isValid("1hgcm82633a004352", null));
    }

    @Test
    public void VIN_with_spaces_should_fail() {
        assertFalse(validator.isValid("1HG CM82633A004352", null));
    }

}
