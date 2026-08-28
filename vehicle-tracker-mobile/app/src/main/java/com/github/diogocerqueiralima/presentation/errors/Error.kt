package com.github.diogocerqueiralima.presentation.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.diogocerqueiralima.R

/**
 * Marker for a reason a view model can move into an error state for. Implemented by [CommonError]
 * and by each feature's own error enum (declared alongside its view model, so it can't be sealed
 * to this package), so a screen can map every possible reason to a message to display.
 */
interface Error

/**
 * Errors that are not specific to a single feature and can be reused across view models.
 */
enum class CommonError : Error {
    NO_ACTIVE_SESSION,
    UNEXPECTED_ERROR
}

/**
 * Default message for a [CommonError], reused by every screen instead of each one mapping these
 * cases on its own. A screen's own `ErrorReason.message()` should delegate to this for the
 * [CommonError] branch and only handle its feature-specific error enum itself.
 */
@Composable
fun CommonError.message(): String = when (this) {
    CommonError.NO_ACTIVE_SESSION -> stringResource(R.string.error_no_active_session)
    CommonError.UNEXPECTED_ERROR -> stringResource(R.string.error_unexpected)
}
