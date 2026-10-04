package com.github.diogocerqueiralima.identity.service.domain.model.certificate

import java.security.PublicKey

/**
 * Represents a Certificate Signing Request (CSR).
 *
 * @property subject the subject information for the certificate to be issued
 * @property publicKey the public key to be included in the certificate
 * @see CertificateSubject
 */
class CertificateSigningRequest(val subject: CertificateSubject, val publicKey: PublicKey)
