package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.CertificateInfo;

/**
 * Port to interact with the certificate data source.
 */
public interface CertificateInfoPersistence {

    /**
     *
     * Saves a certificate to the data source.
     *
     * @param certificateInfo the certificate to be saved
     * @return the saved certificate
     */
    CertificateInfo save(CertificateInfo certificateInfo);

}
