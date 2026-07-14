package com.github.diogocerqueiralima.presentation.authentication.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationState
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationViewModel
import com.github.diogocerqueiralima.presentation.authentication.views.AuthenticationCancelledView
import com.github.diogocerqueiralima.presentation.authentication.views.AuthenticationIdleView
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

/**
 * This screen is responsible for displaying the authentication state of the application before the user is redirected to the authentication page.
 */
@Composable
fun AuthenticationScreen(viewModel: AuthenticationViewModel) {

    VehicleTrackerMobileTheme() {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

            val state = viewModel.state.collectAsState().value

            when (state) {

                is AuthenticationState.Idle -> {
                    AuthenticationIdleView(
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }

                is AuthenticationState.Authenticating -> {
                    AuthenticationIdleView(
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }

                is AuthenticationState.Canceled -> {
                    AuthenticationCancelledView(
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }

            }

        }

    }

}