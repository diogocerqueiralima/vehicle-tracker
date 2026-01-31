package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.BootstrapCertificate;

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

}
