package com.github.diogocerqueiralima.presentation.config;

/**
 * This class holds constants for application URIs.
 */
public class ApplicationURIs {

    // This class should not be instantiated
    private ApplicationURIs() {}

    // Page query parameter names
    public final static String
            PAGE_NUMBER_PARAM = "pageNumber",
            PAGE_SIZE_PARAM = "pageSize";

    // Certificate uri parameter names
    public final static String
            CERTIFICATE_SERIAL_NUMBER_PARAM = "serialNumber";

    // Certificate-related URIs
    public final static String
            CERTIFICATE_BASE_URI = "/certificates",
            CERTIFICATE_SIGNING_REQUEST_URI = CERTIFICATE_BASE_URI + "/sign",
            CERTIFICATE_REVOKE_URI = CERTIFICATE_BASE_URI + "{" + CERTIFICATE_SERIAL_NUMBER_PARAM + "}" + "/revoke";

    // Bootstrap certificate-related URIs
    public final static String
            BOOTSTRAP_CERTIFICATE_BASE_URI = "/certificates/bootstrap",
            BOOTSTRAP_CERTIFICATE_SIGNING_REQUEST_URI = BOOTSTRAP_CERTIFICATE_BASE_URI + "/sign",
            BOOTSTRAP_CERTIFICATE_REVOKE_URI = BOOTSTRAP_CERTIFICATE_BASE_URI + "{" + CERTIFICATE_SERIAL_NUMBER_PARAM + "}" + "/revoke";

}
