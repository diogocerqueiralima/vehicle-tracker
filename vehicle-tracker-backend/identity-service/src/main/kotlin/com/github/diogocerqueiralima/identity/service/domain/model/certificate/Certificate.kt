package com.github.diogocerqueiralima.identity.service.domain.model.certificate

import java.math.BigInteger
import java.time.Instant

/**
 * Represents a certificate in the system. This interface defines the common properties and behaviors of a certificate,
 * such as revocation status, serial number, subject information, and validity period.
 */
interface Certificate {

    /**
     * Whether the certificate has been revoked. A revoked certificate is considered invalid and should not be accepted.
     */
    val isRevoked: Boolean

    /**
     * The serial number of the certificate, a unique identifier assigned to each certificate. It is used to
     * distinguish between different certificates in certificate management and validation processes.
     */
    val serialNumber: BigInteger

    /**
     * The subject information of the certificate, which typically includes details about the entity to which
     * the certificate was issued.
     *
     * @see CertificateSubject
     */
    val subject: CertificateSubject

    /**
     * The timestamp indicating when the certificate was issued. Important for determining the validity period
     * of the certificate and for auditing purposes.
     */
    val issuedAt: Instant

    /**
     * The timestamp indicating when the certificate expires. After this timestamp, the certificate is considered
     * invalid and should not be accepted for authentication or authorization purposes.
     */
    val expiresAt: Instant

    /**
     * Revokes the certificate, marking it as no longer valid for use. Once revoked, the certificate should not be accepted
     * for authentication or authorization purposes.
     *
     * @return the revoked certificate instance, reflecting its new revoked status
     */
    fun revoke(): Certificate

}
