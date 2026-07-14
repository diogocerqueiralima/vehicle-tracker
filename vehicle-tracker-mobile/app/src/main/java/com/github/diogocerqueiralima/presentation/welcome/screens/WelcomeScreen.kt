package com.github.diogocerqueiralima.presentation.welcome.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.diogocerqueiralima.presentation.welcome.views.WelcomeView
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

@Composable
fun WelcomeScreen(onClickJoinPlatform: () -> Unit) {

    VehicleTrackerMobileTheme() {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            WelcomeView(
                modifier = Modifier
                    .padding(innerPadding),
                onClickJoinPlatform = onClickJoinPlatform
            )
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen {}
}