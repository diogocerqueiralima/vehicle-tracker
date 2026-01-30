package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.domain.ports.inbound.CertificateUseCase;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.*;

@RestController
public class CertificateController {

    private static final String CERTIFICATE_FILENAME = "certificate.pem";

    private final CertificateUseCase certificateUseCase;

    public CertificateController(CertificateUseCase certificateUseCase) {
        this.certificateUseCase = certificateUseCase;
    }

    @PostMapping(CERTIFICATE_SIGNING_REQUEST_URI)
    public ResponseEntity<Resource> certificateSigningRequest(@RequestParam("csr") MultipartFile csr) throws IOException {

        CertificateSigningRequestCommand command = new CertificateSigningRequestCommand(csr.getBytes());
        CertificateSigningRequestResult result = certificateUseCase.sign(command);

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

}
