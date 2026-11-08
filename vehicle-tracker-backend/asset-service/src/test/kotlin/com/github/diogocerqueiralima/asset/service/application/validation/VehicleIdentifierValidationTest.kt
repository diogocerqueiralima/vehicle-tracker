package com.github.diogocerqueiralima.asset.service.application.validation

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotBlank
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VehicleIdentifierValidationTest {

    companion object {

        private lateinit var validatorFactory: ValidatorFactory
        private lateinit var validator: Validator

        @JvmStatic
        @BeforeAll
        fun setup() {
            validatorFactory = Validation.buildDefaultValidatorFactory()
            validator = validatorFactory.validator
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            validatorFactory.close()
        }

    }

    @Test
    @DisplayName("Should accept valid VIN and plate")
    fun should_accept_valid_vin_and_plate() {
        val data = VehicleData("1HGCM82633A123456", "AA-00-AA")
        assertThat(validator.validate(data)).isEmpty()
    }

    @Test
    @DisplayName("Should accept legacy Portuguese plate formats")
    fun should_accept_legacy_portuguese_plate_formats() {
        assertThat(validator.validate(VehicleData("1HGCM82633A123456", "12-34-AB"))).isEmpty()
        assertThat(validator.validate(VehicleData("1HGCM82633A123456", "12-AB-34"))).isEmpty()
        assertThat(validator.validate(VehicleData("1HGCM82633A123456", "AB-12-34"))).isEmpty()
    }

    @Test
    @DisplayName("Should reject invalid VIN")
    fun should_reject_invalid_vin() {
        val data = VehicleData("1HGCM82633A12345I", "ABC1234")

        assertThat(validator.validate(data))
            .extracting<String> { it.propertyPath.toString() }
            .contains("vin")
    }

    @Test
    @DisplayName("Should reject invalid plate")
    fun should_reject_invalid_plate() {
        val data = VehicleData("1HGCM82633A123456", "12-INVALID")

        assertThat(validator.validate(data))
            .extracting<String> { it.propertyPath.toString() }
            .contains("plate")
    }

    @Test
    @DisplayName("Should reject plate without Portuguese separators")
    fun should_reject_plate_without_portuguese_separators() {
        val data = VehicleData("1HGCM82633A123456", "AA00AA")

        assertThat(validator.validate(data))
            .extracting<String> { it.propertyPath.toString() }
            .contains("plate")
    }

    private data class VehicleData(
        @get:NotBlank
        @get:VIN
        val vin: String,
        @get:NotBlank
        @get:Plate
        val plate: String
    )

}
