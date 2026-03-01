package com.github.diogocerqueiralima.domain.model;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Represents a certificate in the system. This interface defines the common properties and behaviors of a certificate,
 * such as revocation status, serial number, subject information, and validity period.
 */
public interface Certificate {

    /**
     * Revokes the certificate, marking it as no longer valid for use. Once revoked, the certificate should not be accepted
     * for authentication or authorization purposes.
     *
     * @return the revoked certificate instance, reflecting its new revoked status
     */
    Certificate revoke();

    /**
     *
     * Returns whether the certificate has been revoked. A revoked certificate is considered invalid and should not be accepted
     *
     * @return true if the certificate is revoked, false otherwise
     */
    boolean isRevoked();

    /**
     * Returns the serial number of the certificate, which is a unique identifier assigned to each certificate.
     * The serial number is used to distinguish between different certificates and is often used in certificate management and validation processes.
     *
     * @return the serial number of the certificate
     */
    BigInteger getSerialNumber();

    /**
     * Returns the subject information of the certificate, which typically includes details about the entity to which the certificate was issued.
     *
     * @return the subject information of the certificate
     * @see CertificateSubject
     */
    CertificateSubject getSubject();

    /**
     *
     * Returns the timestamp indicating when the certificate was issued.
     * This information is important for determining the validity period of the certificate and for auditing purposes.
     *
     * @return the timestamp when the certificate was issued
     */
    Instant getIssuedAt();

    /**
     * Returns the timestamp indicating when the certificate expires.
     * After this timestamp, the certificate is considered invalid and should not be accepted for authentication or authorization purposes.
     *
     * @return the timestamp when the certificate expires
     */
    Instant getExpiresAt();

}
