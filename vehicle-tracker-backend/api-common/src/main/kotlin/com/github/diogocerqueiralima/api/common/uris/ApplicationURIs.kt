package com.github.diogocerqueiralima.api.common.uris

/**
 * Constants for asset-service HTTP endpoints. Shared so the api-gateway can reference the
 * same paths (prefixed by the gateway route) instead of hardcoding them.
 */
object ApplicationURIs {

    // Params
    const val PAGE_NUMBER_PARAM = "pageNumber"
    const val PAGE_SIZE_PARAM = "pageSize"

    const val VEHICLE_ID_PARAM = "vehicleId"

    const val DEVICE_ID_PARAM = "deviceId"

    const val SIM_CARD_ID_PARAM = "simCardId"

    // URIs
    const val VEHICLES_BASE_URI = "/vehicles"
    const val VEHICLES_ID_URI = "$VEHICLES_BASE_URI/{$VEHICLE_ID_PARAM}"
    const val VEHICLES_ASSIGNMENTS_BASE_URI = "$VEHICLES_ID_URI/assignments"

    const val DEVICES_BASE_URI = "/devices"
    const val DEVICES_ID_URI = "$DEVICES_BASE_URI/{$DEVICE_ID_PARAM}"

    const val SIM_CARDS_BASE_URI = "/sim-cards"
    const val SIM_CARDS_ID_URI = "$SIM_CARDS_BASE_URI/{$SIM_CARD_ID_PARAM}"
    const val SIM_CARDS_ASSIGNMENTS_BASE_URI = "$SIM_CARDS_ID_URI/assignments"

}
