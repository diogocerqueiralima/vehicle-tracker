package com.github.diogocerqueiralima.presentation.authentication.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.presentation.ui.indicators.ErrorIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.LoadingIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.SuccessIndicator
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

@Composable
fun AuthenticationIdleView(modifier: Modifier = Modifier) {
    AuthenticationView(
        modifier = modifier,
        title = stringResource(R.string.authentication_idle_title),
        subtitle = stringResource(R.string.authentication_idle_subtitle),
        indicator = { LoadingIndicator() }
    )
}

@Composable
fun AuthenticationRedirectView(modifier: Modifier = Modifier) {
    AuthenticationView(
        modifier = modifier,
        title = stringResource(R.string.authentication_redirect_title),
        subtitle = stringResource(R.string.authentication_redirect_subtitle),
        indicator = { LoadingIndicator() }
    )
}

@Composable
fun AuthenticationSuccessView(modifier: Modifier = Modifier) {
    AuthenticationView(
        modifier = modifier,
        title = stringResource(R.string.authentication_success_title),
        subtitle = stringResource(R.string.authentication_success_subtitle),
        indicator = { SuccessIndicator() }
    )
}

@Composable
fun AuthenticationErrorView(modifier: Modifier = Modifier) {
    AuthenticationView(
        modifier = modifier,
        title = stringResource(R.string.authentication_error_title),
        subtitle = stringResource(R.string.authentication_error_subtitle),
        indicator = { ErrorIndicator() }
    )
}

@Composable
private fun AuthenticationView(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    indicator: @Composable () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {

        indicator()

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Text(
                text = subtitle,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )

        }

    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthenticationViewPreview() {
    VehicleTrackerMobileTheme {
        AuthenticationView(
            title = "Authenticating...",
            subtitle = "Please wait while we authenticate your account.",
            indicator = { ErrorIndicator() }
        )
    }
}