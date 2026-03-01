package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.RegularCertificate;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Port to interact with the certificate items source.
 */
public interface CertificatePersistence {

    /**
     *
     * Saves a certificate to the items source.
     *
     * @param certificate the certificate to be saved
     * @return the saved certificate
     */
    RegularCertificate save(RegularCertificate certificate);

    /**
     *
     * Retrieves a certificate by its serial number.
     *
     * @param serialNumber the serial number of the certificate
     * @return an Optional containing the found certificate, or empty if not found
     */
    Optional<RegularCertificate> getBySerialNumber(BigInteger serialNumber);

}
