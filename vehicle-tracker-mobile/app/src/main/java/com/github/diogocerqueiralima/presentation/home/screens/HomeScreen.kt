package com.github.diogocerqueiralima.presentation.home.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.diogocerqueiralima.presentation.home.views.HomeView
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

@Composable
fun HomeScreen(onClickJoinPlatform: () -> Unit) {

    VehicleTrackerMobileTheme() {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HomeView(
                modifier = Modifier
                    .padding(innerPadding),
                onClickJoinPlatform = onClickJoinPlatform
            )
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen {}
}