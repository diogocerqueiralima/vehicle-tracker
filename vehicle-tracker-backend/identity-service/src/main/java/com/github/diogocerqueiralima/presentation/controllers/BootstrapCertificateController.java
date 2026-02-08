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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.*;

@RestController
public class BootstrapCertificateController {

    private static final String CERTIFICATE_FILENAME = "certificate.pem";

    private final BootstrapCertificateUseCase bootstrapCertificateUseCase;

    public BootstrapCertificateController(BootstrapCertificateUseCase bootstrapCertificateUseCase) {
        this.bootstrapCertificateUseCase = bootstrapCertificateUseCase;
    }

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

    @PostMapping(BOOTSTRAP_CERTIFICATE_REVOKE_URI)
    public ResponseEntity<Void> revoke(@PathVariable String serialNumber) {
        bootstrapCertificateUseCase.revoke(new LookupCertificateBySerialNumberCommand(serialNumber));
        return ResponseEntity.ok().build();
    }

}
