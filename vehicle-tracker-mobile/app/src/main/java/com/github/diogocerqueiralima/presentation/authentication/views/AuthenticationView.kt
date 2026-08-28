package com.github.diogocerqueiralima.presentation.authentication.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.presentation.ui.indicators.ErrorIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.LoadingIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.SuccessIndicator
import com.github.diogocerqueiralima.presentation.ui.views.InformationView

/**
 * This view is responsible for displaying the authentication state of the application when the user is waiting for being redirected to the authentication page.
 */
@Composable
fun AuthenticationIdleView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.authentication_idle_title),
        subtitle = stringResource(R.string.authentication_idle_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This view is responsible for displaying the authentication state of the application when the user was redirected to the application after the authentication process.
 */
@Composable
fun AuthenticationRedirectView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.authentication_redirect_title),
        subtitle = stringResource(R.string.authentication_redirect_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This view is responsible for displaying the authentication state of the application when the user has successfully authenticated.
 */
@Composable
fun AuthenticationSuccessView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.authentication_success_title),
        subtitle = stringResource(R.string.authentication_success_subtitle),
        indicator = { SuccessIndicator() }
    )
}

/**
 * This view is responsible for displaying the authentication state of the application when an error occurs.
 */
@Composable
fun AuthenticationErrorView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.authentication_error_title),
        subtitle = stringResource(R.string.authentication_error_subtitle),
        indicator = { ErrorIndicator() }
    )
}

/**
 * This view is responsible for displaying the authentication state of the application when the user cancels the authentication process.
 */
@Composable
fun AuthenticationCancelledView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.authentication_cancelled_title),
        subtitle = stringResource(R.string.authentication_cancelled_subtitle),
        indicator = { LoadingIndicator() }
    )
}
