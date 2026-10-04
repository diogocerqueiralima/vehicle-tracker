package com.github.diogocerqueiralima.identity.service.domain.model.certificate.factories

import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSigningRequest
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.RegularCertificate
import java.math.BigInteger
import java.time.Instant

/**
 * Factory for creating [RegularCertificate] instances from [CertificateSigningRequest] data.
 */
class RegularCertificateFactory : CertificateFactory<RegularCertificate> {

    override fun create(
        csr: CertificateSigningRequest, serialNumber: BigInteger, notBefore: Instant, notAfter: Instant
    ): RegularCertificate = RegularCertificate(serialNumber, csr.subject, notBefore, notAfter, false)

}
