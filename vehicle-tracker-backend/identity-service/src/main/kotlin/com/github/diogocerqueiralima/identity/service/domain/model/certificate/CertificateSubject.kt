package com.github.diogocerqueiralima.identity.service.domain.model.certificate

/**
 * Represents the subject of a certificate, containing information such as Common Name (CN), Organization (O), and Country (C).
 *
 * @property commonName the common name (CN) of the certificate subject
 * @property organization the organization (O) associated with the certificate subject
 * @property country the country (C) associated with the certificate subject
 */
class CertificateSubject(val commonName: String, val organization: String, val country: String) {

    override fun toString(): String = "CN=$commonName, O=$organization, C=$country"

    companion object {

        /**
         * Parser for a Distinguished Name (DN) string in the format "CN=..., O=..., C=...".
         *
         * @param dn the Distinguished Name string to parse
         * @return a [CertificateSubject] instance created from the parsed DN string
         */
        fun fromString(dn: String): CertificateSubject {

            require(dn.isNotEmpty()) { "Distinguished Name cannot be null or empty" }

            val attributes = dn.split(",")
                .map { it.trim().split("=", limit = 2) }
                .filter { it.size == 2 }
                .associate { (key, value) -> key.trim().uppercase() to value.trim() }

            val cn = attributes["CN"]
            val o = attributes["O"]
            val c = attributes["C"]

            require(cn != null && o != null && c != null) { "Distinguished Name must contain CN, O and C" }

            return CertificateSubject(cn, o, c)
        }

    }

}
