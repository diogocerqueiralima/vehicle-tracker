package com.github.diogocerqueiralima.identity.service.domain.model.certificate

import java.math.BigInteger
import java.time.Instant

/**
 * Abstract base class for certificates, providing common properties and methods.
 *
 * @see Certificate
 */
abstract class AbstractCertificate protected constructor(
    override val serialNumber: BigInteger,
    override val subject: CertificateSubject,
    override val issuedAt: Instant,
    override val expiresAt: Instant
) : Certificate
