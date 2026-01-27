package com.github.diogocerqueiralima.presentation.config;

/**
 * This class holds constants for application URIs.
 */
public class ApplicationURIs {

    // This class should not be instantiated
    private ApplicationURIs() {}

    // Certificate-related URIs
    public final static String CERTIFICATE_BASE_URI = "/certificates",
            CERTIFICATE_SIGNING_REQUEST_URI = CERTIFICATE_BASE_URI + "/sign";

}
