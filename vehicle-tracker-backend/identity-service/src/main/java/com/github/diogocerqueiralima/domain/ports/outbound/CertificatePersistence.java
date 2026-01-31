package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.Certificate;

/**
 * Port to interact with the certificate data source.
 */
public interface CertificatePersistence {

    /**
     *
     * Saves a certificate to the data source.
     *
     * @param certificate the certificate to be saved
     * @return the saved certificate
     */
    Certificate save(Certificate certificate);

}
