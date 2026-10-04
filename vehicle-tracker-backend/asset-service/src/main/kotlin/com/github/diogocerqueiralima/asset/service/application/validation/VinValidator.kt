package com.github.diogocerqueiralima.asset.service.application.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * VIN validation using the standard 17-character format.
 */
class VinValidator : ConstraintValidator<VIN, String?> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {

        if (value.isNullOrBlank()) {
            return true
        }

        return VIN_PATTERN.matches(value)
    }

    private companion object {

        val VIN_PATTERN = Regex("^[A-HJ-NPR-Z0-9]{17}$")

    }

}
