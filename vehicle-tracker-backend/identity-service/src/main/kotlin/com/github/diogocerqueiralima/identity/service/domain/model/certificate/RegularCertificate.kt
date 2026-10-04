package com.github.diogocerqueiralima.identity.service.domain.model.certificate

import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.options.RevokeCertificateOptions
import java.math.BigInteger
import java.time.Instant

/**
 * Concrete implementation of a regular certificate, which can be revoked.
 *
 * @see AbstractCertificate
 * @see Certificate
 * @see RevokeCertificateOptions
 */
class RegularCertificate private constructor(
    serialNumber: BigInteger,
    subject: CertificateSubject,
    issuedAt: Instant,
    expiresAt: Instant,
    private val options: RevokeCertificateOptions
) : AbstractCertificate(serialNumber, subject, issuedAt, expiresAt) {

    constructor(
        serialNumber: BigInteger, subject: CertificateSubject, issuedAt: Instant, expiresAt: Instant,
        revoked: Boolean
    ) : this(serialNumber, subject, issuedAt, expiresAt, RevokeCertificateOptions(revoked))

    override val isRevoked: Boolean
        get() = options.isRevoked

    override fun revoke(): Certificate {

        if (!options.validate()) {
            throw OperationFailedException("Certificate with serial number $serialNumber could not be revoked.")
        }

        return RegularCertificate(serialNumber, subject, issuedAt, expiresAt, true)
    }

}
