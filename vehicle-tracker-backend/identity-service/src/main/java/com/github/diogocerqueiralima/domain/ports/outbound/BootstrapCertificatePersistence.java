package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * Port to interact with the bootstrap certificate items source.
 */
public interface BootstrapCertificatePersistence {

    /**
     *
     * Saves a certificate to the items source.
     *
     * @param certificate the certificate to be saved
     * @return the saved certificate
     */
    BootstrapCertificate save(BootstrapCertificate certificate);

    /**
     *
     * Retrieves a bootstrap certificate by its serial number.
     *
     * @param serialNumber the serial number of the certificate
     * @return an Optional containing the found certificate, or empty if not found
     */
    Optional<BootstrapCertificate> getBySerialNumber(String serialNumber);

    /**
     *
     * Retrieves a paginated list of bootstrap certificates.
     *
     * @param page the page number to retrieve (0-based index)
     * @param pageSize the number of certificates to include in each page
     * @return a paginated list of bootstrap certificates for the specified page and page size
     */
    Page<BootstrapCertificate> getPage(int page, int pageSize);

}
