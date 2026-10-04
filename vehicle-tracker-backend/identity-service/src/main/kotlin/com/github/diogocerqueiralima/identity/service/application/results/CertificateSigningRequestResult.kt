package com.github.diogocerqueiralima.identity.service.application.results

import java.math.BigInteger

/**
 * @property serialNumber the serial number of the issued certificate
 * @property subject the subject of the issued certificate
 * @property data the issued certificate, PEM encoded
 */
data class CertificateSigningRequestResult(
    val serialNumber: BigInteger,
    val subject: String,
    val data: String
)
