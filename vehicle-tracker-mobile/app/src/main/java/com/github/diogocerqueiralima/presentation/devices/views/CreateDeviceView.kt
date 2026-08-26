package com.github.diogocerqueiralima.presentation.devices.views

import android.view.ScaleGestureDetector
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.domain.model.Device
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.time.Instant

/**
 * This view is responsible for scanning a device's QR code and decoding it into a device
 * identifier. Camera permission is granted/requested by the caller (the hosting activity); this
 * view only renders based on the state it is given and reports scan results back.
 *
 * @param modifier Modifier to be applied to the view.
 * @param hasCameraPermission Whether the camera permission has been granted.
 * @param invalidCodeScanned Whether the last scanned code could not be recognized as a device id.
 * @param onRequestCameraPermission Callback invoked when the user requests the camera permission.
 * @param onQrDecoded Callback invoked with the decoded device id, or null if the scanned code was invalid.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
fun ScanDeviceQrView(
    modifier: Modifier = Modifier,
    hasCameraPermission: Boolean,
    invalidCodeScanned: Boolean,
    onRequestCameraPermission: () -> Unit,
    onQrDecoded: (UUID?) -> Unit
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnQrDecoded by rememberUpdatedState(onQrDecoded)

    Column(modifier = modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = stringResource(R.string.scan_device_qr_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )

            if (invalidCodeScanned) {
                Text(
                    text = stringResource(R.string.scan_device_qr_invalid_code),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

        }

        if (!hasCameraPermission) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    Text(
                        text = stringResource(R.string.scan_device_qr_camera_permission_denied),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(onClick = onRequestCameraPermission) {
                        Text(text = stringResource(R.string.scan_device_qr_grant_permission))
                    }

                }

            }

        } else {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->

                    val previewView = PreviewView(context)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    val analysisExecutor = Executors.newSingleThreadExecutor()
                    val barcodeScanner = BarcodeScanning.getClient(
                        BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()
                    )

                    cameraProviderFuture.addListener(
                        {

                            val cameraProvider = cameraProviderFuture.get()

                            val preview = CameraPreview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                        processImageProxy(barcodeScanner, imageProxy, currentOnQrDecoded)
                                    }
                                }

                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )

                            // Small QR codes are hard to focus on at 1x; start closer in.
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

                            previewView.setOnTouchListener { _, event ->
                                scaleGestureDetector.onTouchEvent(event)
                                true
                            }

                        },
                        ContextCompat.getMainExecutor(context)
                    )

                    previewView
                }
            )

        }

    }

}

@ExperimentalGetImage
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onResult: (UUID?) -> Unit
) {

    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->

            val rawValue = barcodes.firstOrNull()?.rawValue
            if (rawValue != null) {

                val deviceId = try {
                    UUID.fromString(rawValue)
                } catch (exception: IllegalArgumentException) {
                    null
                }

                onResult(deviceId)
            }

        }
        .addOnCompleteListener {
            imageProxy.close()
        }

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
    id: UUID,
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
            value = id.toString(),
            onValueChange = {},
            enabled = false,
            label = { Text(text = stringResource(R.string.create_device_id_label)) },
            singleLine = true
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
 * @param message Error message from the failed submission.
 * @param onSubmit Callback invoked when the form is submitted.
 */
@Composable
fun CreateDeviceErrorView(
    modifier: Modifier = Modifier,
    id: UUID,
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
            id = id,
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
                message = "Failed to create device.",
                onSubmit = {}
            )
        }
    }
}
