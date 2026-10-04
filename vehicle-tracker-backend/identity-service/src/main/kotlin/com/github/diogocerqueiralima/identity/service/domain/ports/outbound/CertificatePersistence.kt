package com.github.diogocerqueiralima.identity.service.domain.ports.outbound

import com.github.diogocerqueiralima.identity.service.domain.model.certificate.RegularCertificate
import java.math.BigInteger

/**
 * Port to interact with the certificate items source.
 */
interface CertificatePersistence {

    /**
     * Saves a certificate to the items source.
     *
     * @param certificate the certificate to be saved
     * @return the saved certificate
     */
    fun save(certificate: RegularCertificate): RegularCertificate

    /**
     * Retrieves a certificate by its serial number.
     *
     * @param serialNumber the serial number of the certificate
     * @return the found certificate, or null if not found
     */
    fun getBySerialNumber(serialNumber: BigInteger): RegularCertificate?

}
