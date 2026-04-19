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

    public static final String DEVICE_ID_PARAM = "id",
            DEVICE_PAGE_NUMBER_PARAM = "pageNumber",
            DEVICE_PAGE_SIZE_PARAM = "pageSize";

    // URIs
    public static final String VEHICLES_BASE_URI = "/vehicles",
            VEHICLES_ID_URI = VEHICLES_BASE_URI + "/{" + VEHICLE_ID_PARAM + "}",
            VEHICLES_ASSIGNMENTS_BASE_URI = VEHICLES_BASE_URI + "/assignments";

    public static final String DEVICES_BASE_URI = "/devices",
            DEVICES_ID_URI = DEVICES_BASE_URI + "/{" + DEVICE_ID_PARAM + "}";

}

