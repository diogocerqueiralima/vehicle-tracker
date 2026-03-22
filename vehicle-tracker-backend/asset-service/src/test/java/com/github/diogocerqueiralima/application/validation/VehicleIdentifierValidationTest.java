package com.github.diogocerqueiralima.application.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleIdentifierValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void teardown() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("Should accept valid VIN and plate")
    void should_accept_valid_vin_and_plate() {
        VehicleData data = new VehicleData("1HGCM82633A123456", "AA-00-AA");
        assertThat(validator.validate(data)).isEmpty();
    }

    @Test
    @DisplayName("Should accept legacy Portuguese plate formats")
    void should_accept_legacy_portuguese_plate_formats() {
        assertThat(validator.validate(new VehicleData("1HGCM82633A123456", "12-34-AB"))).isEmpty();
        assertThat(validator.validate(new VehicleData("1HGCM82633A123456", "12-AB-34"))).isEmpty();
        assertThat(validator.validate(new VehicleData("1HGCM82633A123456", "AB-12-34"))).isEmpty();
    }

    @Test
    @DisplayName("Should reject invalid VIN")
    void should_reject_invalid_vin() {
        VehicleData data = new VehicleData("1HGCM82633A12345I", "ABC1234");

        assertThat(validator.validate(data))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("vin");
    }

    @Test
    @DisplayName("Should reject invalid plate")
    void should_reject_invalid_plate() {
        VehicleData data = new VehicleData("1HGCM82633A123456", "12-INVALID");

        assertThat(validator.validate(data))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("plate");
    }

    @Test
    @DisplayName("Should reject plate without Portuguese separators")
    void should_reject_plate_without_portuguese_separators() {
        VehicleData data = new VehicleData("1HGCM82633A123456", "AA00AA");

        assertThat(validator.validate(data))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("plate");
    }

    private record VehicleData(
            @NotBlank
            @VIN
            String vin,
            @NotBlank
            @Plate
            String plate
    ) {}

}


