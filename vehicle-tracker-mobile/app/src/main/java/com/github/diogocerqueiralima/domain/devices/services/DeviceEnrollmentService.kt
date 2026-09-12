package com.github.diogocerqueiralima.domain.devices.services

import android.util.Log
import com.github.diogocerqueiralima.domain.devices.catalog.Catalog
import com.github.diogocerqueiralima.domain.devices.connection.DeviceConnection
import com.github.diogocerqueiralima.domain.devices.repositories.CertificateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "DEVICE_ENROLLMENT_SERVICE"

/**
 * A step of the enrollment flow, in the order they run.
 */
enum class EnrollmentStep {

    /** Reading the certificate signing request from the device. */
    READING_REQUEST,

    /** Having the Identity Service sign the request. */
    SIGNING,

    /** Writing the issued certificate back to the device. */
    INSTALLING

}

/**
 * Service responsible for enrolling a connected device: it reads the request the device generated,
 * has the Identity Service sign it, and installs the certificate back on the device.
 *
 * The user never handles either PEM. The app is already signed in as the user the device is
 * assigned to, which is the trust the Identity Service relies on when signing.
 */
class DeviceEnrollmentService(
    private val deviceConnection: DeviceConnection,
    private val certificateRepository: CertificateRepository
) {

    /**
     * Runs the enrollment end to end, emitting each [EnrollmentStep] as it starts, so a caller can
     * report which step is running and which one a failure came from. Completing without an
     * exception means the certificate is installed.
     *
     * Nothing is written to the device before the certificate is issued, so a failure leaves the
     * device as it was. The device keeps serving the same request across reads, so the whole flow
     * can be collected again to retry it.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun enroll(): Flow<EnrollmentStep> = flow {

        // 1. Read the request the device generated for its own key pair.
        emit(EnrollmentStep.READING_REQUEST)
        Log.d(TAG, "Reading the certificate signing request from the device")

        val csr = deviceConnection.read(Catalog.csr.serviceId, Catalog.csr.characteristicId)

        // 2. Have it signed on behalf of the signed-in user.
        emit(EnrollmentStep.SIGNING)
        Log.d(TAG, "Submitting the certificate signing request, ${csr.size} bytes")

        val signed = certificateRepository.sign(csr)

        // 3. Install the issued certificate, unchanged, on the device that asked for it.
        emit(EnrollmentStep.INSTALLING)
        Log.d(TAG, "Installing the issued certificate, ${signed.size} bytes")

        deviceConnection.write(Catalog.certificate.serviceId, Catalog.certificate.characteristicId, signed)

        Log.d(TAG, "Device enrolled successfully")
    }

}
