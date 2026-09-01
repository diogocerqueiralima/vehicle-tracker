package com.github.diogocerqueiralima.presentation.devices.views

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.presentation.ui.indicators.ErrorIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.LoadingIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.SuccessIndicator
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import com.github.diogocerqueiralima.presentation.ui.views.InformationView
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.time.Instant

/**
 * This view is responsible for scanning a device's QR code and decoding it into a device
 * identifier. Camera permission is granted/requested by the caller (the hosting activity); this
 * view only renders based on the state it is given and reports scan results back.
 *
 * @param modifier Modifier to be applied to the view.
 * @param cameraProvider The camera provider to be used for scanning the QR code.
 * @param processImage Callback invoked when a new camera frame is available for processing.
 * @param onCameraUnavailable Callback invoked when the camera could not be bound, e.g. because the device has none.
 */
@Composable
fun ScanDeviceQrView(
    modifier: Modifier = Modifier,
    cameraProvider: ListenableFuture<ProcessCameraProvider>,
    processImage: (ImageProxy, BarcodeScanner) -> Unit,
    onCameraUnavailable: () -> Unit = {}
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->

            val previewView = PreviewView(context).apply {

                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                this.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                this.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            cameraProvider.addListener({

                val provider = cameraProvider.get()

                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                        .also { it.setAnalyzer(analysisExecutor) { proxy -> processImage(proxy, scanner) } }

                provider.unbindAll()
                val camera = try {
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: IllegalArgumentException) {
                    onCameraUnavailable()
                    return@addListener
                }

                val maxZoomRatio = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
                camera.cameraControl.setZoomRatio(minOf(2f, maxZoomRatio))

                val scaleGestureDetector = ScaleGestureDetector(
                    context,
                    object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        override fun onScale(detector: ScaleGestureDetector): Boolean {

                            val zoomState = camera.cameraInfo.zoomState.value ?: return true
                            val newZoomRatio = zoomState.zoomRatio * detector.scaleFactor

                            camera.cameraControl.setZoomRatio(
                                newZoomRatio.coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)
                            )

                            return true
                        }
                    }
                )

                previewView.setOnTouchListener { view, event ->

                    scaleGestureDetector.onTouchEvent(event)

                    if (event.action == MotionEvent.ACTION_UP) {
                        view.performClick()
                    }

                    true
                }

            }, ContextCompat.getMainExecutor(context))

            previewView

        },
        onRelease = {

            if (cameraProvider.isDone) {
                cameraProvider.get().unbindAll()
            }

            scanner.close()
            analysisExecutor.shutdown()
        }
    )

}

/**
 * This view is displayed while the device creation flow is starting up, before the camera
 * permission state has been checked.
 *
 * @param modifier Modifier to be applied to the view.
 */
@Composable
fun IdleView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.getting_ready_title),
        subtitle = stringResource(R.string.please_wait_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This view represents the device creation form, collecting device details to be submitted.
 *
 * @param modifier Modifier to be applied to the view.
 * @param id Identifier of the device, scanned from its QR code. Not editable.
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
    id: UUID?,
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

        if (id != null) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = id.toString(),
                onValueChange = {},
                enabled = false,
                label = { Text(text = stringResource(R.string.create_device_id_label)) },
                singleLine = true
            )
        }

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
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.create_device_submitting_title),
        subtitle = stringResource(R.string.create_device_submitting_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This view is displayed when an error occurs during the device creation flow.
 *
 * @param modifier Modifier to be applied to the view.
 * @param message Error message describing what went wrong.
 */
@Composable
fun CreateDeviceErrorView(modifier: Modifier = Modifier, message: String) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.create_device_error_title),
        subtitle = message,
        indicator = { ErrorIndicator() }
    )
}

/**
 * This view is displayed once a device has been successfully created.
 *
 * @param modifier Modifier to be applied to the view.
 * @param device The device that was created.
 */
@Composable
fun CreateDeviceSuccessView(modifier: Modifier = Modifier, device: Device) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.create_device_success_title),
        subtitle = device.displayName,
        indicator = { SuccessIndicator() }
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun CreateDeviceViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxWidth()) { innerPadding ->
            CreateDeviceView(
                modifier = Modifier.padding(innerPadding),
                id = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
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
fun IdleViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxWidth()) { innerPadding ->
            IdleView(modifier = Modifier.padding(innerPadding))
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
                    id = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
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
                message = "Failed to create device."
            )
        }
    }
}
