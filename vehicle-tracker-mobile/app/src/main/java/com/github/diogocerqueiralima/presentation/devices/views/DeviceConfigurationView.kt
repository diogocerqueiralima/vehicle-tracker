package com.github.diogocerqueiralima.presentation.devices.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.domain.devices.catalog.Catalog
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicSpec
import com.github.diogocerqueiralima.domain.devices.catalog.ServiceSpec
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CharacteristicFailureReason
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CharacteristicValueState
import com.github.diogocerqueiralima.presentation.ui.indicators.ErrorIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.LoadingIndicator
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import com.github.diogocerqueiralima.presentation.ui.views.InformationView
import java.util.UUID
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

/**
 * This view is displayed while the device configuration flow is starting up, before a device has
 * been given to connect to.
 *
 * @param modifier Modifier to be applied to the view.
 */
@Composable
fun DeviceConfigurationIdleView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.getting_ready_title),
        subtitle = stringResource(R.string.please_wait_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This view is displayed while the device is being connected to.
 *
 * @param modifier Modifier to be applied to the view.
 */
@Composable
fun DeviceConfigurationConnectingView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.device_configuration_connecting_title),
        subtitle = stringResource(R.string.device_configuration_connecting_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This view is displayed once the device has been successfully connected to. It lists every
 * known GATT service, grouped into collapsible sections, each listing its characteristics.
 *
 * @param modifier Modifier to be applied to the view.
 * @param device The device that was connected to.
 * @param characteristicValues Current read state for each characteristic, keyed by [CharacteristicSpec.key].
 * @param onExpandService Callback invoked when a service section is expanded, so its
 * characteristics can be read.
 * @param onWriteCharacteristic Callback invoked when the user submits a new value for a
 * writable characteristic.
 */
@Composable
fun DeviceConfigurationConnectedView(
    modifier: Modifier = Modifier,
    device: Device,
    characteristicValues: Map<String, CharacteristicValueState> = emptyMap(),
    onExpandService: (ServiceSpec) -> Unit = {},
    onWriteCharacteristic: (CharacteristicSpec, String) -> Unit = { _, _ -> }
) {

    var editingCharacteristic by remember { mutableStateOf<CharacteristicSpec?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(Catalog.services) { service ->
            ServiceSection(
                service = service,
                characteristicValues = characteristicValues,
                onExpand = { onExpandService(service) },
                onCharacteristicClick = { editingCharacteristic = it }
            )
        }

    }

    editingCharacteristic?.let { characteristic ->
        CharacteristicEditDialog(
            characteristic = characteristic,
            initialValue = (characteristicValues[characteristic.key] as? CharacteristicValueState.Loaded)?.value ?: "",
            onDismiss = { editingCharacteristic = null },
            onConfirm = { value ->
                onWriteCharacteristic(characteristic, value)
                editingCharacteristic = null
            }
        )
    }

}

/**
 * Collapsible card for a single GATT service, listing its characteristics once expanded.
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
private fun ServiceSection(
    service: ServiceSpec,
    characteristicValues: Map<String, CharacteristicValueState>,
    onExpand: () -> Unit,
    onCharacteristicClick: (CharacteristicSpec) -> Unit
) {

    var expanded by rememberSaveable(service.uuid) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        if (expanded) onExpand()
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    service.characteristics.forEach { characteristic ->
                        CharacteristicRow(
                            characteristic = characteristic,
                            state = characteristicValues[characteristic.key],
                            onClick = { onCharacteristicClick(characteristic) }
                        )
                    }
                }
            }

        }

    }

}

/**
 * Row displaying a single characteristic's name, description, current read state, and its
 * access mode. Writable characteristics can be tapped to open an editor for their value.
 */
@Composable
private fun CharacteristicRow(
    characteristic: CharacteristicSpec,
    state: CharacteristicValueState?,
    onClick: () -> Unit
) {

    val accessLabel = when {
        characteristic.readable && characteristic.writable -> stringResource(R.string.device_configuration_characteristic_read_write)
        characteristic.writable -> stringResource(R.string.device_configuration_characteristic_write_only)
        else -> stringResource(R.string.device_configuration_characteristic_read_only)
    }

    val valueText = when (state) {
        is CharacteristicValueState.Loaded -> state.value
        CharacteristicValueState.Loading -> stringResource(R.string.device_configuration_characteristic_loading)
        is CharacteristicValueState.Failed -> when (state.reason) {
            CharacteristicFailureReason.NOT_CONFIGURED -> stringResource(R.string.device_configuration_characteristic_not_configured)
            CharacteristicFailureReason.ACCESS_FAILED -> stringResource(R.string.device_configuration_characteristic_failed)
        }
        null -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (characteristic.writable) it.clickable(onClick = onClick) else it },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = characteristic.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = characteristic.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (valueText != null) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

        }

        Text(
            text = accessLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
        )

    }

}

/**
 * Dialog for entering a new value to write to a writable characteristic.
 *
 * @param characteristic The characteristic being edited.
 * @param initialValue The value to prefill the input with, if any.
 * @param onDismiss Callback invoked when the dialog is dismissed without confirming.
 * @param onConfirm Callback invoked with the entered value when the user confirms.
 */
@Composable
private fun CharacteristicEditDialog(
    characteristic: CharacteristicSpec,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {

    var value by rememberSaveable(characteristic.key) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = characteristic.name) },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = { value = it },
                label = { Text(text = characteristic.description) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                enabled = value.isNotBlank()
            ) {
                Text(text = stringResource(R.string.device_configuration_characteristic_edit_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.device_configuration_characteristic_edit_cancel))
            }
        }
    )

}

/**
 * This view is displayed when an error occurs during the device configuration flow.
 *
 * @param modifier Modifier to be applied to the view.
 * @param message Error message describing what went wrong.
 */
@Composable
fun DeviceConfigurationErrorView(modifier: Modifier = Modifier, message: String) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.device_configuration_error_title),
        subtitle = message,
        indicator = { ErrorIndicator() }
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DeviceConfigurationIdleViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DeviceConfigurationIdleView(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DeviceConfigurationConnectingViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DeviceConfigurationConnectingView(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DeviceConfigurationConnectedViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DeviceConfigurationConnectedView(
                modifier = Modifier.padding(innerPadding),
                device = Device(
                    id = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                    createdAt = Instant.parse("2024-01-15T10:30:00Z"),
                    updatedAt = Instant.parse("2024-06-01T08:00:00Z"),
                    serialNumber = "SN-00123456",
                    model = "TrackPro X200",
                    manufacturer = "Teltonika",
                    imei = "352099001761481"
                ),
                characteristicValues = mapOf(
                    Catalog.services[0].characteristics[0].key to CharacteristicValueState.Loaded("mqtt://broker.local:1883"),
                    Catalog.services[0].characteristics[1].key to CharacteristicValueState.Loading,
                    Catalog.services[0].characteristics[2].key to CharacteristicValueState.Failed(CharacteristicFailureReason.NOT_CONFIGURED)
                )
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DeviceConfigurationErrorViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DeviceConfigurationErrorView(
                modifier = Modifier.padding(innerPadding),
                message = "Failed to connect to the device."
            )
        }
    }
}
