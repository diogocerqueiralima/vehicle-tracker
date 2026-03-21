package com.github.diogocerqueiralima.presentation.config;

/**
 * Constants for asset-service HTTP endpoints.
 */
public class ApplicationURIs {

    private ApplicationURIs() {}

    // Params
    public static final String VEHICLE_ID_PARAM = "id",
            VEHICLE_PAGE_NUMBER_PARAM = "pageNumber",
            VEHICLE_PAGE_SIZE_PARAM = "pageSize";

    // URIs
    public static final String VEHICLES_BASE_URI = "/vehicles",
            VEHICLES_ID_URI = VEHICLES_BASE_URI + "/{" + VEHICLE_ID_PARAM + "}";

}

