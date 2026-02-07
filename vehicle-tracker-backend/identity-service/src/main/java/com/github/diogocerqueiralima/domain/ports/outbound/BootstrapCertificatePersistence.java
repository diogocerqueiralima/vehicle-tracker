package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;

import java.util.Optional;

/**
 * Port to interact with the bootstrap certificate data source.
 */
public interface BootstrapCertificatePersistence {

    /**
     *
     * Saves a certificate to the data source.
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

}
