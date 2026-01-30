package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.CertificateUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.*;

@RestController
public class CertificateController {

    private final CertificateUseCase certificateUseCase;

    public CertificateController(CertificateUseCase certificateUseCase) {
        this.certificateUseCase = certificateUseCase;
    }

    @PostMapping(CERTIFICATE_SIGNING_REQUEST_URI)
    public ResponseEntity<Void> certificateSigningRequest(@RequestParam("csr") MultipartFile csr) throws IOException {

        CertificateSigningRequestCommand command = new CertificateSigningRequestCommand(csr.getBytes());
        certificateUseCase.sign(command);

        return ResponseEntity.ok().build();
    }

}
