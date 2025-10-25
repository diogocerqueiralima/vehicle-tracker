package com.github.diogocerqueiralima.application.validators;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class YearValidatorTest {

    private final Year.YearValidator validator = new Year.YearValidator();

    @Test
    public void year_older_than_1886_should_fail() {
        assertFalse(validator.isValid(1885, null));
    }

    @Test
    public void year_in_the_future_should_fail() {
        int nextYear = java.time.Year.now().getValue() + 1;
        assertFalse(validator.isValid(nextYear, null));
    }

    @Test
    public void year_1886_should_pass() {
        assertTrue(validator.isValid(1886, null));
    }

    @Test
    public void current_year_should_pass() {
        int currentYear = java.time.Year.now().getValue();
        assertTrue(validator.isValid(currentYear, null));
    }

    @Test
    public void year_between_1886_and_current_year_should_pass() {
        int midYear = (1886 + java.time.Year.now().getValue()) / 2;
        assertTrue(validator.isValid(midYear, null));
    }

    @Test
    public void null_year_should_pass() {
        assertTrue(validator.isValid(null, null));
    }

}
