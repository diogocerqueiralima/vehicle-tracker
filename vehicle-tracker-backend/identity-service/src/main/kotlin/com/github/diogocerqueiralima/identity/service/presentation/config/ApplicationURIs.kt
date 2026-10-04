package com.github.diogocerqueiralima.identity.service.presentation.config

/**
 * This object holds constants for application URIs.
 */
object ApplicationURIs {

    // Certificate uri parameter names
    const val CERTIFICATE_SERIAL_NUMBER_PARAM = "serialNumber"

    // Certificate-related URIs
    const val CERTIFICATE_BASE_URI = "/certificates"
    const val CERTIFICATE_SIGNING_REQUEST_URI = "$CERTIFICATE_BASE_URI/sign"
    const val CERTIFICATE_REVOKE_URI = "$CERTIFICATE_BASE_URI/{$CERTIFICATE_SERIAL_NUMBER_PARAM}/revoke"

    // CRL Distribution Point URI
    const val CRL_DISTRIBUTION_POINT_URI = "/crl"

}
