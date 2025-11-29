package com.github.diogocerqueiralima.presentation.ui.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

@Composable
fun LocationIcon(modifier: Modifier = Modifier) {

    Icon(
        imageVector = Icons.Filled.LocationOn,
        contentDescription = "Location Icon",
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .padding(8.dp)
            .size(24.dp)
    )

}

@Composable
@Preview
fun LocationIconPreview() {
    VehicleTrackerMobileTheme {
        LocationIcon()
    }
}