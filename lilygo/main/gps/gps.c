#include "gps.h"

const static char *TAG = "GPS";

int gps_init(esp_modem_dce_t *modem) {
    
    char response[128];
    esp_err_t ret;

    int error_code = gps_disable(modem);
    if (error_code != GPS_SUCCESS) {
        return error_code;
    }

    ret = esp_modem_at_raw(modem, "AT+CGPSURL=\"supl.google.com:7275\"\r", response, "OK", "ERROR", 1000);
    if (ret != ESP_OK) {
        return GPS_SET_SUPL_URL_ERROR;
    }

    ret = esp_modem_at_raw(modem, "AT+CGPSSSL=1\r", NULL, "OK", "ERROR", 1000);
    if (ret != ESP_OK) {
        return GPS_SET_SSL_ERROR;
    }

    ret = esp_modem_at_raw(modem, "AT+CGPSXE=1\r", response, "OK", "ERROR", 1000);
    if (ret != ESP_OK) {
        return GPS_SET_XTRA_FUNCTION_ERROR;
    }

    ret = esp_modem_at_raw(modem, "AT+CGPSXDAUTO=1\r", NULL, "OK", "ERROR", 30000);
    if (ret != ESP_OK) {
        return GPS_GET_ASSIST_DATA_ERROR;
    }

    ret = esp_modem_at_raw(modem, "AT+CGPSXD=0\r", response, "+CGPSXD: 0", "ERROR", 1000);
    if (ret != ESP_OK) {
        return GPS_GET_ASSIST_DATA_ERROR;
    }

    esp_modem_at_raw(modem, "AT+CGPSPMD=773\r", response, "OK", "ERROR", 1000);
    ESP_LOGI(TAG, "CGPSPMD response: %s", response);

    esp_modem_at_raw(modem, "AT+CGPSPMD?\r", response, "OK", "ERROR", 1000);
    ESP_LOGI(TAG, "CGPSPMD response: %s", response);

    ret = esp_modem_at_raw(modem, "AT+CGPSXTRADATA?\r", response, "OK", "ERROR", 1000);
    ESP_LOGI(TAG, "XTRA data status: %s", response);

    return GPS_SUCCESS;
}

int gps_enable(esp_modem_dce_t *modem) {

    esp_err_t ret;

    ret = esp_modem_at_raw(modem, "AT+CGPS=1,3\r", NULL, "OK", "ERROR", 1000);
    if (ret != ESP_OK) {
        return GPS_ENABLE_ERROR;
    }

    return GPS_SUCCESS;
}

int gps_disable(esp_modem_dce_t *modem) {

    esp_err_t ret;

    ret = esp_modem_at_raw(modem, "AT+CGPS=0\r", NULL, "OK", "ERROR", 1000);
    if (ret != ESP_OK) {
        return GPS_DISABLE_ERROR;
    }

    return GPS_SUCCESS;
}

int gps_get_mode(gps_status_t *status, esp_modem_dce_t *modem) {

    esp_err_t ret;
    char response[128];

    ret = esp_modem_at_raw(modem, "AT+CGPS?\r", response, "OK", "ERROR", 1000);
    if (ret != ESP_OK) {
        return GPS_GET_MODE_ERROR;
    }

    char *first_line = strtok(response, "\r\n");
    int power, mode;

    if (sscanf(first_line, "+CGPS: %d,%d", &power, &mode) != 2) {
        return GPS_GET_MODE_ERROR;
    }

    status->power = (gps_power_t) power;
    status->mode = (gps_mode_t) mode;

    return GPS_SUCCESS;
}

int gps_get_data(gps_data_t *data, esp_modem_dce_t *modem) {
    
    char response[256];
    esp_err_t ret;

    ret = esp_modem_at_raw(modem, "AT+CGNSSINFO\r", response, "OK", "ERROR", 2000);
    if (ret != ESP_OK) {
        return GPS_GET_DATA_ERROR;
    }

    ESP_LOGI(TAG, "CGNSSINFO response: %s", response);

    char *line = strtok(response, "\r\n");
    if (line == NULL || strstr(line, "+CGNSSINFO: ,,,,,,,,,,,,,,,") != NULL) {
        return GPS_FIX_NOT_READY;
    }

    // Parse +CGNSSINFO response
    // Example: +CGNSSINFO: 2,01,01,03,3845.909705,N,00918.991986,W,211225,181347.0,217.4,0.0,,2.4,2.2,1.0

    sscanf(line,
        "+CGNSSINFO: %hhu,%hhu,%hhu,%hhu,%15[^,],%c,%15[^,],%c,%lu,%lf,%f,%f,%f,%f,%f,%f",
        &data->mode,
        &data->satelliteType,
        &data->fixStatus,
        &data->satellitesUsed,
        data->latitude,
        &data->latHemisphere,
        data->longitude,
        &data->lonHemisphere,
        &data->date,
        &data->timeUTC,
        &data->altitude,
        &data->speed,
        &data->heading,
        &data->hdop,
        &data->pdop,
        &data->vdop
    );

    return GPS_SUCCESS;
}

char *gps_get_error_message(int code) {
    switch (code) {
        case GPS_SUCCESS:
            return "Success";
        case GPS_SET_SUPL_URL_ERROR:
            return "Failed to set SUPL server URL";
        case GPS_SET_XTRA_FUNCTION_ERROR:
            return "Failed to set XTRA function";
        case GPS_SET_SSL_ERROR:
            return "Failed to enable SSL for GPS";
        case GPS_GET_ASSIST_DATA_ERROR:
            return "Failed to get assisted GPS data";
        case GPS_ENABLE_ERROR:
            return "Failed to enable GPS";
        case GPS_DISABLE_ERROR:
            return "Failed to disable GPS";
        case GPS_GET_MODE_ERROR:
            return "Failed to get GPS mode";
        case GPS_GET_DATA_ERROR:
            return "Failed to get GPS data";
        case GPS_FIX_NOT_READY:
            return "GPS fix not ready";
        default:
            return "Unknown error";
    }
}