package com.github.diogocerqueiralima.asset.service.application.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * Plate validation supporting Portuguese formats.
 */
class PlateValidator : ConstraintValidator<Plate, String?> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {

        if (value.isNullOrBlank()) {
            return true
        }

        return PT_PLATE_PATTERN.matches(value)
    }

    private companion object {

        // PT plates must use 3 groups of 2 chars separated by hyphens.
        // Supported generations: 00-00-AA, 00-AA-00, AA-00-00, AA-00-AA.
        val PT_PLATE_PATTERN =
            Regex("^(?:[0-9]{2}-[0-9]{2}-[A-Z]{2}|[0-9]{2}-[A-Z]{2}-[0-9]{2}|[A-Z]{2}-[0-9]{2}-[0-9]{2}|[A-Z]{2}-[0-9]{2}-[A-Z]{2})$")

    }

}
