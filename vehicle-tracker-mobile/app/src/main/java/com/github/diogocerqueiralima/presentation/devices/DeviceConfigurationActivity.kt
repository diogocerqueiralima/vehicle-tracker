package com.github.diogocerqueiralima.presentation.devices

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.github.diogocerqueiralima.DependenciesContainer
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.domain.devices.services.DeviceConfigurationService
import com.github.diogocerqueiralima.infrastructure.devices.connection.BluetoothDeviceConnection
import com.github.diogocerqueiralima.infrastructure.devices.scanner.BluetoothDeviceScanner
import com.github.diogocerqueiralima.presentation.devices.screens.DeviceConfigurationScreen
import com.github.diogocerqueiralima.presentation.devices.viewmodel.DeviceConfigurationViewModel
import com.github.diogocerqueiralima.presentation.devices.viewmodel.DeviceConfigurationViewModelFactory
import java.util.UUID
import kotlin.time.Instant

private const val EXTRA_DEVICE_ID = "device_id"
private const val EXTRA_DEVICE_CREATED_AT = "device_created_at"
private const val EXTRA_DEVICE_UPDATED_AT = "device_updated_at"
private const val EXTRA_DEVICE_SERIAL_NUMBER = "device_serial_number"
private const val EXTRA_DEVICE_MODEL = "device_model"
private const val EXTRA_DEVICE_MANUFACTURER = "device_manufacturer"
private const val EXTRA_DEVICE_IMEI = "device_imei"

/**
 * Activity that handles connecting to and configuring a device.
 */
class DeviceConfigurationActivity : ComponentActivity() {

    companion object {

        fun intent(context: Context, device: Device): Intent =
            Intent(context, DeviceConfigurationActivity::class.java).apply {
                putExtra(EXTRA_DEVICE_ID, device.id.toString())
                putExtra(EXTRA_DEVICE_CREATED_AT, device.createdAt.toString())
                putExtra(EXTRA_DEVICE_UPDATED_AT, device.updatedAt.toString())
                putExtra(EXTRA_DEVICE_SERIAL_NUMBER, device.serialNumber)
                putExtra(EXTRA_DEVICE_MODEL, device.model)
                putExtra(EXTRA_DEVICE_MANUFACTURER, device.manufacturer)
                putExtra(EXTRA_DEVICE_IMEI, device.imei)
            }

    }

    private val viewModel by viewModels<DeviceConfigurationViewModel>(
        factoryProducer = {

            val dependenciesContainer = application as DependenciesContainer
            val deviceScanner = BluetoothDeviceScanner(dependenciesContainer.bluetoothManager)
            val deviceConnection = BluetoothDeviceConnection(applicationContext, dependenciesContainer.bluetoothManager)
            val deviceConfigurationService = DeviceConfigurationService(deviceScanner, deviceConnection)

            DeviceConfigurationViewModelFactory(deviceConfigurationService)
        }
    )

    private val device: Device by lazy {
        Device(
            id = UUID.fromString(intent.getStringExtra(EXTRA_DEVICE_ID)),
            createdAt = Instant.parse(intent.getStringExtra(EXTRA_DEVICE_CREATED_AT)!!),
            updatedAt = Instant.parse(intent.getStringExtra(EXTRA_DEVICE_UPDATED_AT)!!),
            serialNumber = intent.getStringExtra(EXTRA_DEVICE_SERIAL_NUMBER)!!,
            model = intent.getStringExtra(EXTRA_DEVICE_MODEL)!!,
            manufacturer = intent.getStringExtra(EXTRA_DEVICE_MANUFACTURER)!!,
            imei = intent.getStringExtra(EXTRA_DEVICE_IMEI)!!
        )
    }

    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    private val bluetoothPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        viewModel.onBluetoothPermissionResult(results.values.all { it }, device)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (bluetoothPermissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            viewModel.connect(device)
        } else {
            bluetoothPermissionsLauncher.launch(bluetoothPermissions)
        }

        setContent {
            DeviceConfigurationScreen(viewModel = viewModel, onBack = { finish() })
        }

    }

}
