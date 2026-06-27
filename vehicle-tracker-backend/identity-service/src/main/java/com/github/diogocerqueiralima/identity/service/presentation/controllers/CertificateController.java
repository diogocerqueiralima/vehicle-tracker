package com.github.diogocerqueiralima.identity.service.presentation.controllers;

import com.github.diogocerqueiralima.identity.service.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.identity.service.application.commands.LookupCertificateBySerialNumberCommand;
import com.github.diogocerqueiralima.identity.service.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.identity.service.domain.ports.inbound.CertificateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

import static com.github.diogocerqueiralima.identity.service.presentation.config.ApplicationURIs.*;

@Tag(name = "Certificates", description = "Operations related to certificates, including signing and revocation.")
@SecurityRequirements(value = { @SecurityRequirement(name = "bearerAuth") })
@RestController
public class CertificateController {

    private static final String CERTIFICATE_FILENAME = "certificate.pem";

    private final CertificateUseCase certificateUseCase;

    public CertificateController(CertificateUseCase certificateUseCase) {
        this.certificateUseCase = certificateUseCase;
    }

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
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully processed the certificate signing request and issued a signed certificate"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The certificate signing request is invalid or malformed"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required and has failed or has not yet been provided"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the certificate signing request"
                    )
            }
    )
    @PostMapping(CERTIFICATE_SIGNING_REQUEST_URI)
    public ResponseEntity<Resource> certificateSigningRequest(
            @RequestParam("csr") MultipartFile csr, JwtAuthenticationToken authentication
    ) throws IOException {

        // 1. Resolves the authenticated user id from Keycloak token subject.
        UUID userId = extractUserId(authentication);

        // 2. Delegates the certificate signing request to the application layer.
        CertificateSigningRequestCommand command = new CertificateSigningRequestCommand(csr.getBytes(), userId);
        CertificateSigningRequestResult result = certificateUseCase.sign(command);
        Resource resource = new ByteArrayResource(result.data());

        // 3. Returns the signed certificate as a downloadable file in the response.
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + CERTIFICATE_FILENAME + "\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
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
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully revoked the certificate identified by the provided serial number"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The provided serial number is invalid or malformed"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required and has failed or has not yet been provided"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "The server understood the request but refuses to authorize it"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No certificate found with the provided serial number"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the certificate revocation request"
                    )
            }
    )
    @PostMapping(CERTIFICATE_REVOKE_URI)
    public ResponseEntity<Void> revoke(@PathVariable BigInteger serialNumber) {
        certificateUseCase.revoke(new LookupCertificateBySerialNumberCommand(serialNumber));
        return ResponseEntity.ok().build();
    }

    private UUID extractUserId(JwtAuthenticationToken authentication) {

        // 1. Keycloak stores the user id in token subject claim.
        String subject = authentication.getToken().getSubject();

        // 2. Converts subject to UUID used by application/domain contracts.
        return UUID.fromString(subject);
    }

}
