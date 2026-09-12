package com.github.diogocerqueiralima.domain.devices.repositories

/**
 * Interface for operations related to the certificates issued for a device's key pair.
 */
interface CertificateRepository {

    /**
     * Submits [csr] to the Identity Service to be signed on behalf of the signed-in user, and
     * returns the certificate issued for it.
     *
     * Both PEMs are carried as raw bytes, unchanged in either direction: the device signs the
     * exact request it generated, and validates the exact certificate that comes back.
     *
     * @param csr The PEM-encoded certificate signing request read from the device.
     * @return The PEM-encoded certificate issued for [csr].
     * @throws com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException if the
     * request is rejected or the certificate cannot be issued.
     */
    suspend fun sign(csr: ByteArray): ByteArray

}
