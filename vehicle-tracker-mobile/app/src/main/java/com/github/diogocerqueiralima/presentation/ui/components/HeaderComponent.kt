package com.github.diogocerqueiralima.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.diogocerqueiralima.presentation.ui.icons.LocationIcon
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

@Composable
fun HeaderComponent(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {

    Surface(
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
    ) {

        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
        ) {

            Row(
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                icon()

                Column() {

                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                }

            }

        }

    }

}

@Composable
@Preview(showBackground = true)
fun HeaderComponentPreview() {
    VehicleTrackerMobileTheme(darkTheme = true) {
        HeaderComponent(
            icon = { LocationIcon() },
            title = "Os meus carros",
            description = "10 veículos"
        )
    }
}