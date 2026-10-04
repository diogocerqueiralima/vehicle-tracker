package com.github.diogocerqueiralima.identity.service.application.services

import com.github.diogocerqueiralima.error.common.exceptions.BadRequestException
import com.github.diogocerqueiralima.error.common.exceptions.ForbiddenException
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.identity.service.application.commands.CertificateSigningRequestCommand
import com.github.diogocerqueiralima.identity.service.application.commands.LookupCertificateBySerialNumberCommand
import com.github.diogocerqueiralima.identity.service.application.config.CertificateAuthorityConfig
import com.github.diogocerqueiralima.identity.service.application.results.CertificateSigningRequestResult
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.Certificate
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSubject
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.factories.CertificateFactory
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.CertificateSigner
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.CertificateSigningRequestReader
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.DeviceProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

private val log = LoggerFactory.getLogger(CertificateService::class.java)

@Service
class CertificateService(
    private val certificateSigner: CertificateSigner,
    private val certificateSigningRequestReader: CertificateSigningRequestReader,
    private val certificateAuthorityConfig: CertificateAuthorityConfig,
    private val deviceProvider: DeviceProvider
) {

    /**
     * Signs a certificate signing request and persists the resulting certificate.
     *
     * @param command the certificate signing request command containing the CSR details
     * @param persistence a function to persist the Certificate
     * @param factory a factory to create a Certificate from the signing request
     * @return the result of the certificate signing request, including the signed certificate items
     */
    fun <T : Certificate> sign(
        command: CertificateSigningRequestCommand,
        persistence: (T) -> T,
        factory: CertificateFactory<T>
    ): CertificateSigningRequestResult {

        // 1. Parses the certificate signing request command into a CertificateSigningRequest object
        val certificateSigningRequest = certificateSigningRequestReader.read(command.value)
        log.info("Parsed certificate signing request for Subject={}", certificateSigningRequest.subject)

        // 2. Validates that the certificate subject contains a valid device ID.
        val deviceId = getDeviceIdFromCertificateSubject(certificateSigningRequest.subject)
            ?: throw BadRequestException("Common Name must be a valid UUID representing the device ID")

        // 3. Validates that the device exists.
        val device = deviceProvider.findById(deviceId) ?: throw NotFoundException("Device with id $deviceId not found")

        // 4. Validates that the user making the request is the owner of the device.
        if (device.ownerId != command.userId) {
            throw ForbiddenException("Device with id $deviceId does not belong to owner with id ${command.userId}")
        }

        // 5. Generates the serial number, notBefore and notAfter timestamps for the certificate based on the CA configuration
        val serialNumber = generateSerialNumber()
        val notBefore = Instant.now()
        val notAfter = notBefore.plusSeconds(certificateAuthorityConfig.validityDays * 24L * 60L * 60L)

        // 6. Sign certificate
        val certificateData = certificateSigner.sign(
            certificateSigningRequest,
            certificateAuthorityConfig.issuer,
            serialNumber,
            notBefore,
            notAfter
        )

        // 7. Create and persists the certificate
        val certificate = persistence(factory.create(certificateSigningRequest, serialNumber, notBefore, notAfter))
        log.info("Persisted certificate with SerialNumber={}", certificate.serialNumber)

        // 8. Build result
        return CertificateSigningRequestResult(
            certificate.serialNumber,
            certificate.subject.toString(),
            certificateData
        )

    }

    /**
     * Revokes a certificate by its serial number. It looks up the certificate using the provided finder function,
     * and if found, it revokes the certificate and persists the updated state using the provided persistence function.
     *
     * @param command the command containing the serial number of the certificate to be revoked
     * @param finder a function to find the certificate by its serial number, returning null if there is none
     * @param persistence a function to persist the revoked certificate
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Certificate> revoke(
        command: LookupCertificateBySerialNumberCommand,
        finder: (BigInteger) -> T?,
        persistence: (T) -> T
    ) {
        val certificate = finder(command.serialNumber)
            ?: throw NotFoundException("Certificate with serial number ${command.serialNumber} not found.")

        persistence(certificate.revoke() as T)
    }

    /**
     * Generates a random serial number for the certificate.
     *
     * @return the generated serial number
     */
    private fun generateSerialNumber(): BigInteger = BigInteger(120, SecureRandom())

    /**
     * Extracts the device ID from the certificate subject's common name. It attempts to parse the common name as a UUID.
     *
     * @param subject the certificate subject containing the common name to be parsed
     * @return the device ID if parsing is successful, or null if parsing fails
     */
    private fun getDeviceIdFromCertificateSubject(subject: CertificateSubject): UUID? =
        try {
            UUID.fromString(subject.commonName)
        } catch (e: IllegalArgumentException) {
            null
        }

}
