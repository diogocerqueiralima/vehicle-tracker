package com.github.diogocerqueiralima.application.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
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
    void shouldAcceptValidVinAndPlate() {
        VehicleData data = new VehicleData("1HGCM82633A123456", "AA-00-AA");
        assertThat(validator.validate(data)).isEmpty();
    }

    @Test
    void shouldAcceptLegacyPortuguesePlateFormats() {
        assertThat(validator.validate(new VehicleData("1HGCM82633A123456", "12-34-AB"))).isEmpty();
        assertThat(validator.validate(new VehicleData("1HGCM82633A123456", "12-AB-34"))).isEmpty();
        assertThat(validator.validate(new VehicleData("1HGCM82633A123456", "AB-12-34"))).isEmpty();
    }

    @Test
    void shouldRejectInvalidVin() {
        VehicleData data = new VehicleData("1HGCM82633A12345I", "ABC1234");

        assertThat(validator.validate(data))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("vin");
    }

    @Test
    void shouldRejectInvalidPlate() {
        VehicleData data = new VehicleData("1HGCM82633A123456", "12-INVALID");

        assertThat(validator.validate(data))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("plate");
    }

    @Test
    void shouldRejectPlateWithoutPortugueseSeparators() {
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


