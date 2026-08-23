package com.github.diogocerqueiralima.presentation.devices.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.domain.model.Device
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import kotlin.time.Instant

/**
 * This view represents the device creation form, collecting device details to be submitted.
 *
 * @param modifier Modifier to be applied to the view.
 * @param serialNumber Current value of the serial number field.
 * @param onSerialNumberChange Callback invoked when the serial number field changes.
 * @param model Current value of the model field.
 * @param onModelChange Callback invoked when the model field changes.
 * @param manufacturer Current value of the manufacturer field.
 * @param onManufacturerChange Callback invoked when the manufacturer field changes.
 * @param imei Current value of the imei field.
 * @param onImeiChange Callback invoked when the imei field changes.
 * @param isValid Whether the form can be submitted.
 * @param onSubmit Callback invoked when the form is submitted.
 */
@Composable
fun CreateDeviceView(
    modifier: Modifier = Modifier,
    serialNumber: String,
    onSerialNumberChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    manufacturer: String,
    onManufacturerChange: (String) -> Unit,
    imei: String,
    onImeiChange: (String) -> Unit,
    isValid: Boolean,
    onSubmit: () -> Unit
) {

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = stringResource(R.string.create_device_title),
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = serialNumber,
            onValueChange = onSerialNumberChange,
            label = { Text(text = stringResource(R.string.create_device_serial_number_label)) },
            singleLine = true
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = model,
            onValueChange = onModelChange,
            label = { Text(text = stringResource(R.string.create_device_model_label)) },
            singleLine = true
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = manufacturer,
            onValueChange = onManufacturerChange,
            label = { Text(text = stringResource(R.string.create_device_manufacturer_label)) },
            singleLine = true
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = imei,
            onValueChange = onImeiChange,
            label = { Text(text = stringResource(R.string.create_device_imei_label)) },
            singleLine = true
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isValid,
            onClick = onSubmit
        ) {
            Text(text = stringResource(R.string.create_device_submit))
        }

    }

}

/**
 * This view is displayed while a device creation request is in flight.
 *
 * @param modifier Modifier to be applied to the view.
 */
@Composable
fun CreateDeviceSubmittingView(modifier: Modifier = Modifier) {

    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

}

/**
 * This view represents the device creation form after a failed submission, showing the error
 * alongside the editable fields so the user can correct and retry.
 *
 * @param modifier Modifier to be applied to the view.
 * @param serialNumber Current value of the serial number field.
 * @param onSerialNumberChange Callback invoked when the serial number field changes.
 * @param model Current value of the model field.
 * @param onModelChange Callback invoked when the model field changes.
 * @param manufacturer Current value of the manufacturer field.
 * @param onManufacturerChange Callback invoked when the manufacturer field changes.
 * @param imei Current value of the imei field.
 * @param onImeiChange Callback invoked when the imei field changes.
 * @param isValid Whether the form can be submitted.
 * @param message Error message from the failed submission.
 * @param onSubmit Callback invoked when the form is submitted.
 */
@Composable
fun CreateDeviceErrorView(
    modifier: Modifier = Modifier,
    serialNumber: String,
    onSerialNumberChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    manufacturer: String,
    onManufacturerChange: (String) -> Unit,
    imei: String,
    onImeiChange: (String) -> Unit,
    isValid: Boolean,
    message: String,
    onSubmit: () -> Unit
) {

    Column(modifier = modifier) {

        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        CreateDeviceView(
            serialNumber = serialNumber,
            onSerialNumberChange = onSerialNumberChange,
            model = model,
            onModelChange = onModelChange,
            manufacturer = manufacturer,
            onManufacturerChange = onManufacturerChange,
            imei = imei,
            onImeiChange = onImeiChange,
            isValid = isValid,
            onSubmit = onSubmit
        )

    }

}

/**
 * This view is displayed once a device has been successfully created.
 *
 * @param modifier Modifier to be applied to the view.
 * @param device The device that was created.
 */
@Composable
fun CreateDeviceSuccessView(modifier: Modifier = Modifier, device: Device) {

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.create_device_success_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = device.displayName,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

    }

}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun CreateDeviceViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxWidth()) { innerPadding ->
            CreateDeviceView(
                modifier = Modifier.padding(innerPadding),
                serialNumber = "SN-00123456",
                onSerialNumberChange = {},
                model = "TrackPro X200",
                onModelChange = {},
                manufacturer = "Teltonika",
                onManufacturerChange = {},
                imei = "352099001761481",
                onImeiChange = {},
                isValid = true,
                onSubmit = {}
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun CreateDeviceSubmittingViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxWidth()) { innerPadding ->
            CreateDeviceSubmittingView(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun CreateDeviceSuccessViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxWidth()) { innerPadding ->
            CreateDeviceSuccessView(
                modifier = Modifier.padding(innerPadding),
                device = Device(
                    createdAt = Instant.parse("2024-01-15T10:30:00Z"),
                    updatedAt = Instant.parse("2024-06-01T08:00:00Z"),
                    serialNumber = "SN-00123456",
                    model = "TrackPro X200",
                    manufacturer = "Teltonika",
                    imei = "352099001761481"
                )
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun CreateDeviceErrorViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxWidth()) { innerPadding ->
            CreateDeviceErrorView(
                modifier = Modifier.padding(innerPadding),
                serialNumber = "SN-00123456",
                onSerialNumberChange = {},
                model = "TrackPro X200",
                onModelChange = {},
                manufacturer = "Teltonika",
                onManufacturerChange = {},
                imei = "352099001761481",
                onImeiChange = {},
                isValid = true,
                message = "Failed to create device.",
                onSubmit = {}
            )
        }
    }
}
