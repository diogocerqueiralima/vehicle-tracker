package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.commands.LookupCertificateBySerialNumberCommand;
import com.github.diogocerqueiralima.application.commands.RetrievePageCommand;
import com.github.diogocerqueiralima.application.results.BootstrapCertificateResult;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.domain.ports.inbound.BootstrapCertificateUseCase;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.BootstrapCertificateDTO;
import com.github.diogocerqueiralima.presentation.dto.PageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.*;

@Tag(
        name = "Bootstrap Certificates",
        description = "Operations related to boostrap certificates, including retrieval, signing, and revocation."
)
@SecurityRequirement(name = "bearerAuth")
@RestController
public class BootstrapCertificateController {

    private static final String CERTIFICATE_FILENAME = "certificate.pem";

    private final BootstrapCertificateUseCase bootstrapCertificateUseCase;

    public BootstrapCertificateController(BootstrapCertificateUseCase bootstrapCertificateUseCase) {
        this.bootstrapCertificateUseCase = bootstrapCertificateUseCase;
    }

    @Operation(
            summary = "Retrieves a paginated list of bootstrap certificates.",
            description = """
                    Fetches a paginated list of bootstrap certificates based on the provided pagination parameters.
                    The response includes the current page number, total pages, and a list of bootstrap certificates with their details.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved bootstrap certificates page"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required and has failed or has not yet been provided"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the request"
                    )
            }
    )
    @GetMapping(BOOTSTRAP_CERTIFICATE_BASE_URI)
    public ResponseEntity<ApiResponseDTO<PageDTO<BootstrapCertificateDTO>>> getPage(
            @RequestParam(name = PAGE_NUMBER_PARAM) int pageNumber,
            @RequestParam(name = PAGE_SIZE_PARAM) int pageSize
    ) {

        PageResult<BootstrapCertificateResult> result = bootstrapCertificateUseCase.getPage(
                new RetrievePageCommand(pageNumber, pageSize)
        );

        PageDTO<BootstrapCertificateDTO> dto = new PageDTO<>(
                result.currentPage(),
                result.totalPages(),
                result.data().stream()
                        .map(bootstrapCertificateResult ->
                                new BootstrapCertificateDTO(
                                        bootstrapCertificateResult.serialNumber(),
                                        bootstrapCertificateResult.subject(),
                                        bootstrapCertificateResult.issuedAt(),
                                        bootstrapCertificateResult.expiresAt(),
                                        bootstrapCertificateResult.revoked(),
                                        bootstrapCertificateResult.used()
                                )
                        )
                        .toList()
        );

        return ResponseEntity
                .ok(new ApiResponseDTO<>("Successfully retrieved bootstrap certificates page", dto));
    }

    @Operation(
            summary = "Processes a certificate signing request and issues a signed certificate.",
            description = """
                    Accepts a certificate signing request (CSR) in the form of a multipart file upload,
                    processes it, and returns the signed certificate as a downloadable file.
                    The response includes appropriate headers to indicate the content type and disposition
                    for downloading the signed certificate.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully processed certificate signing request and issued signed certificate"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid certificate signing request provided"
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
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the certificate signing request"
                    )
            }
    )
    @PostMapping(BOOTSTRAP_CERTIFICATE_SIGNING_REQUEST_URI)
    public ResponseEntity<Resource> certificateSigningRequest(@RequestParam("csr") MultipartFile csr) throws IOException {

        CertificateSigningRequestCommand command = new CertificateSigningRequestCommand(csr.getBytes());
        CertificateSigningRequestResult result = bootstrapCertificateUseCase.sign(command);
        Resource resource = new ByteArrayResource(result.data());

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
            summary = "Revokes a bootstrap certificate based on its serial number.",
            description = """
                    Revokes a bootstrap certificate identified by its serial number.
                    This operation marks the certificate as revoked, preventing it from being used for authentication
                    or other purposes. The response indicates the success of the revocation operation.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully revoked the bootstrap certificate"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid serial number provided for revocation"
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
                            description = "No bootstrap certificate found with the provided serial number"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the revocation request"
                    )
            }
    )
    @PostMapping(BOOTSTRAP_CERTIFICATE_REVOKE_URI)
    public ResponseEntity<Void> revoke(@PathVariable String serialNumber) {
        bootstrapCertificateUseCase.revoke(new LookupCertificateBySerialNumberCommand(serialNumber));
        return ResponseEntity.ok().build();
    }

}
