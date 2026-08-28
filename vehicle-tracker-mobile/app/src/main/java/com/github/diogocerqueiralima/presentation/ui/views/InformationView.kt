package com.github.diogocerqueiralima.presentation.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.diogocerqueiralima.presentation.ui.indicators.ErrorIndicator
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

/**
 * This view is responsible for displaying an indicator alongside a title and subtitle,
 * used to communicate the state of a process to the user.
 *
 * @param modifier Modifier to be applied to the view.
 * @param title Title describing the current state.
 * @param subtitle Subtitle giving more detail about the current state.
 * @param indicator Indicator representing the current state.
 */
@Composable
fun InformationView(
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
fun InformationViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            InformationView(
                modifier = Modifier.padding(innerPadding),
                title = "Authenticating...",
                subtitle = "Please wait while we authenticate your account.",
                indicator = { ErrorIndicator() }
            )
        }
    }
}
