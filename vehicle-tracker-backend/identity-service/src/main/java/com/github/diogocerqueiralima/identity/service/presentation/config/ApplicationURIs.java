package com.github.diogocerqueiralima.identity.service.presentation.config;

/**
 * This class holds constants for application URIs.
 */
public class ApplicationURIs {

    // This class should not be instantiated
    private ApplicationURIs() {}

    // Certificate uri parameter names
    public final static String
            CERTIFICATE_SERIAL_NUMBER_PARAM = "serialNumber";

    // Certificate-related URIs
    public final static String
            CERTIFICATE_BASE_URI = "/certificates",
            CERTIFICATE_SIGNING_REQUEST_URI = CERTIFICATE_BASE_URI + "/sign",
            CERTIFICATE_REVOKE_URI = CERTIFICATE_BASE_URI + "/{" + CERTIFICATE_SERIAL_NUMBER_PARAM + "}" + "/revoke";

    // CRL Distribution Point URI
    public final static String
            CRL_DISTRIBUTION_POINT_URI = "/crl";

}
