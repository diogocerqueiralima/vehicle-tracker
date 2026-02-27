package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.commands.LookupCertificateBySerialNumberCommand;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Interface defining the contract for certificate-related use cases.
 */
@Validated
public interface CertificateUseCase {

    /**
     *
     * Receives a certificate signing request and processes it to issue a signed certificate.
     *
     * @param command the certificate signing request command containing the CSR details
     * @return the result of the certificate signing request, including the signed certificate items
     */
    CertificateSigningRequestResult sign(@Valid CertificateSigningRequestCommand command);

    /**
     *
     * Revokes a certificate based on its serial number.
     *
     * @param command the command containing the serial number of the certificate to be revoked
     */
    void revoke(@Valid LookupCertificateBySerialNumberCommand command);

}
