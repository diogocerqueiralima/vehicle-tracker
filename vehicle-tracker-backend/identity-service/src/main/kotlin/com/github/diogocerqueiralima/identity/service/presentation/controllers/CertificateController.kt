package com.github.diogocerqueiralima.identity.service.presentation.controllers

import com.github.diogocerqueiralima.api.common.headers.ReservedHeaders
import com.github.diogocerqueiralima.identity.service.application.commands.CertificateSigningRequestCommand
import com.github.diogocerqueiralima.identity.service.application.commands.LookupCertificateBySerialNumberCommand
import com.github.diogocerqueiralima.identity.service.domain.ports.inbound.CertificateUseCase
import com.github.diogocerqueiralima.identity.service.presentation.config.ApplicationURIs.CERTIFICATE_REVOKE_URI
import com.github.diogocerqueiralima.identity.service.presentation.config.ApplicationURIs.CERTIFICATE_SERIAL_NUMBER_PARAM
import com.github.diogocerqueiralima.identity.service.presentation.config.ApplicationURIs.CERTIFICATE_SIGNING_REQUEST_URI
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.math.BigInteger
import java.util.UUID

private const val CERTIFICATE_FILENAME = "certificate.pem"

@Tag(name = "Certificates", description = "Operations related to certificates, including signing and revocation.")
@SecurityRequirements(value = [SecurityRequirement(name = "bearerAuth")])
@RestController
class CertificateController(private val certificateUseCase: CertificateUseCase) {

    @Operation(
        summary = "Processes a certificate signing request (CSR) and issues a signed certificate.",
        description = """
            Accepts a certificate signing request (CSR) in the form of a multipart file upload, processes it,
            and returns the signed certificate as a downloadable file.
            The response includes the signed certificate data in PEM format,
            which can be used for secure communications.
            """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully processed the certificate signing request and issued a signed certificate"
            ),
            ApiResponse(
                responseCode = "400",
                description = "The certificate signing request is invalid or malformed"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication is required and has failed or has not yet been provided"
            ),
            ApiResponse(
                responseCode = "500",
                description = "An unexpected error occurred while processing the certificate signing request"
            )
        ]
    )
    @PostMapping(CERTIFICATE_SIGNING_REQUEST_URI)
    fun certificateSigningRequest(
        @RequestParam("csr") csr: MultipartFile,
        @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String
    ): ResponseEntity<Resource> {

        // 1. Resolves the authenticated user id from the user ID header.
        val userId = UUID.fromString(userIdHeader)

        // 2. Delegates the certificate signing request to the application layer.
        val command = CertificateSigningRequestCommand(String(csr.bytes, Charsets.UTF_8), userId)
        val result = certificateUseCase.sign(command)
        val resource = ByteArrayResource(result.data.toByteArray(Charsets.UTF_8))

        // 3. Returns the signed certificate as a downloadable file in the response.
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$CERTIFICATE_FILENAME\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(resource.contentLength())
            .body(resource)
    }

    @Operation(
        summary = "Revokes a certificate based on its serial number.",
        description = """
            Revokes a certificate identified by its serial number. This operation marks the certificate as revoked,
            preventing it from being used for secure communications. The serial number is provided as a path variable
            in the request URI.
            """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully revoked the certificate identified by the provided serial number"
            ),
            ApiResponse(
                responseCode = "400",
                description = "The provided serial number is invalid or malformed"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication is required and has failed or has not yet been provided"
            ),
            ApiResponse(
                responseCode = "403",
                description = "The server understood the request but refuses to authorize it"
            ),
            ApiResponse(
                responseCode = "404",
                description = "No certificate found with the provided serial number"
            ),
            ApiResponse(
                responseCode = "500",
                description = "An unexpected error occurred while processing the certificate revocation request"
            )
        ]
    )
    @PostMapping(CERTIFICATE_REVOKE_URI)
    fun revoke(@PathVariable(CERTIFICATE_SERIAL_NUMBER_PARAM) serialNumber: BigInteger): ResponseEntity<Void> {
        certificateUseCase.revoke(LookupCertificateBySerialNumberCommand(serialNumber))
        return ResponseEntity.ok().build()
    }

}
