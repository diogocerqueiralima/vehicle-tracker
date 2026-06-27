package com.github.diogocerqueiralima.asset.service.presentation.http.config;

/**
 * Constants for asset-service HTTP endpoints.
 */
public class ApplicationURIs {

    private ApplicationURIs() {}

    // Params
    public static final String PAGE_NUMBER_PARAM = "pageNumber",
        PAGE_SIZE_PARAM = "pageSize";

    public static final String VEHICLE_ID_PARAM = "vehicleId";

    public static final String DEVICE_ID_PARAM = "deviceId";

    public static final String SIM_CARD_ID_PARAM = "simCardId";

    // URIs
    public static final String VEHICLES_BASE_URI = "/vehicles",
            VEHICLES_ID_URI = VEHICLES_BASE_URI + "/{" + VEHICLE_ID_PARAM + "}",
            VEHICLES_ASSIGNMENTS_BASE_URI = VEHICLES_ID_URI + "/assignments";

    public static final String DEVICES_BASE_URI = "/devices",
            DEVICES_ID_URI = DEVICES_BASE_URI + "/{" + DEVICE_ID_PARAM + "}";

    public static final String SIM_CARDS_BASE_URI = "/sim-cards",
            SIM_CARDS_ID_URI = SIM_CARDS_BASE_URI + "/{" + SIM_CARD_ID_PARAM + "}",
            SIM_CARDS_ASSIGNMENTS_BASE_URI = SIM_CARDS_ID_URI + "/assignments";

}

