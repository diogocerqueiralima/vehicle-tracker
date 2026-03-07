package com.github.diogocerqueiralima.presentation.authentication.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.RedirectState
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.RedirectViewModel
import com.github.diogocerqueiralima.presentation.authentication.views.AuthenticationCancelledView
import com.github.diogocerqueiralima.presentation.authentication.views.AuthenticationErrorView
import com.github.diogocerqueiralima.presentation.authentication.views.AuthenticationRedirectView
import com.github.diogocerqueiralima.presentation.authentication.views.AuthenticationSuccessView
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

@Composable
fun RedirectScreen(viewModel: RedirectViewModel) {

    VehicleTrackerMobileTheme() {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

            val state = viewModel.state.collectAsState().value

            when (state) {

                is RedirectState.Redirected -> {
                    AuthenticationRedirectView(
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }

                is RedirectState.Authenticated -> {
                    AuthenticationSuccessView(
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }

                is RedirectState.Error -> {
                    AuthenticationErrorView(
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }

            }

        }

    }

}