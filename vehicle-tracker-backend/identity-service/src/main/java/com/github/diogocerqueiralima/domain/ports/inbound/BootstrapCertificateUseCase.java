package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.CertificateSigningRequestCommand;
import com.github.diogocerqueiralima.application.commands.LookupCertificateBySerialNumberCommand;
import com.github.diogocerqueiralima.application.commands.MarkBootstrapCertificateAsUsedCommand;
import com.github.diogocerqueiralima.application.commands.RetrievePageCommand;
import com.github.diogocerqueiralima.application.results.BootstrapCertificateResult;
import com.github.diogocerqueiralima.application.results.CertificateSigningRequestResult;
import com.github.diogocerqueiralima.application.exceptions.CertificateNotFoundException;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;
import com.github.diogocerqueiralima.domain.exceptions.BootstrapCertificateUsedException;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Interface defining the contract for boostrap certificate-related use cases.
 */
@Validated
public interface BootstrapCertificateUseCase {

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
     * Marks a bootstrap certificate as used based on its serial number.
     *
     * @param command the command containing the serial number of the certificate to be marked as used
     * @throws CertificateNotFoundException if the certificate with the given serial number does not exist
     * @throws BootstrapCertificateUsedException if the certificate has already been marked as used
     * @throws CertificateRevokedException if the certificate has been revoked
     */
    void markAsUsed(@Valid MarkBootstrapCertificateAsUsedCommand command);

    /**
     *
     * Revokes a certificate based on its serial number.
     *
     * @param command the command containing the serial number of the certificate to be revoked
     */
    void revoke(@Valid LookupCertificateBySerialNumberCommand command);

    /**
     *
     * Retrieves a paginated list of bootstrap certificates based on the provided pagination and filtering criteria.
     *
     * @param command the command containing pagination and filtering criteria for retrieving bootstrap certificates
     * @return a paginated result containing the list of bootstrap certificates matching the criteria
     */
    PageResult<BootstrapCertificateResult> getPage(@Valid RetrievePageCommand command);

}