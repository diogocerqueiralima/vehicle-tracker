package com.github.diogocerqueiralima.presentation.home.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.presentation.home.views.HomeView
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationBar
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationDestination
import com.github.diogocerqueiralima.presentation.ui.components.HeaderComponent
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

/**
 * This screen is responsible for displaying the home screen of the application.
 * It includes a header, a bottom navigation bar, and the home view.
 *
 * @param onNavigate Callback to be invoked when a bottom navigation destination is selected.
 */
@Composable
fun HomeScreen(onNavigate: (BottomNavigationDestination) -> Unit = {}) {

    VehicleTrackerMobileTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                HeaderComponent(
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    },
                    title = stringResource(R.string.home_title),
                    description = stringResource(R.string.home_subtitle)
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedDestination = BottomNavigationDestination.Home,
                    onDestinationSelected = onNavigate
                )
            }
        ) { innerPadding ->
            HomeView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
