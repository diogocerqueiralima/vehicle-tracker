#ifndef GPS_H
#define GPS_H

#include <stdio.h>
#include <stdbool.h>
#include <string.h>

#include "esp_modem_api.h"
#include "esp_log.h"

#include "../utils/at.h"

#define GPS_SUCCESS                         0
#define GPS_SET_SUPL_URL_ERROR              1
#define GPS_SET_XTRA_FUNCTION_ERROR         2
#define GPS_SET_SSL_ERROR                   3
#define GPS_GET_ASSIST_DATA_ERROR           4
#define GPS_ENABLE_ERROR                    5
#define GPS_DISABLE_ERROR                   6
#define GPS_GET_MODE_ERROR                  7
#define GPS_GET_DATA_ERROR                  8
#define GPS_FIX_NOT_READY                   9
#define GPS_CONFIGURE_POSITION_MODE_ERROR   10

/**
 * @brief GPS data structure.
 * 
 * Stores the GPS data retrieved from the modem.
 */
typedef struct {

    uint8_t mode;            // GNSS mode (e.g., 2 = GPS fix)
    uint8_t satelliteType;   // Type of satellite (01 = GPS, 02 = GLONASS, etc.)
    uint8_t fixStatus;       // Fix status (01 = 3D fix valid)
    uint8_t satellitesUsed;  // Number of satellites used for the fix
    char latitude[16];       // Latitude as raw string in DDMM.MMMM format
    char latHemisphere;      // Latitude hemisphere: 'N' or 'S'
    char longitude[16];      // Longitude as raw string in DDDMM.MMMM format
    char lonHemisphere;      // Longitude hemisphere: 'E' or 'W'
    uint32_t date;           // Date in DDMMYY format
    double timeUTC;          // UTC time in hhmmss.sss format
    float altitude;          // Altitude in meters
    float speed;             // Speed over ground
    float heading;           // Heading/course in degrees
    float hdop;              // Horizontal Dilution of Precision
    float pdop;              // Positional Dilution of Precision (3D)
    float vdop;              // Vertical Dilution of Precision

} gps_data_t;

/**
 * @brief GPS power status enumeration.
 */
typedef enum {
    GPS_OFF = 0,
    GPS_ON = 1,
} gps_power_t;

/**
 * @brief GPS operation mode enumeration.
 */
typedef enum {
    GPS_STANDALONE = 1,
    GPS_BASED = 2,
    GPS_ASSISTED = 3
} gps_mode_t;

/**
 * @brief GPS status structure.
 * 
 * Stores the current power state and operation mode of the GPS.
 */
typedef struct {
    gps_power_t power;
    gps_mode_t mode;
} gps_status_t;

/**
 * @brief Initializes the GPS functionality on the modem. Configure AGPS settings.
 * 
 * @param modem Pointer to the esp_modem_dce_t structure representing the modem.
 * @return GPS_SUCCESS on success, error code otherwise.
 */
int gps_init(esp_modem_dce_t *modem);

/**
 * @brief Enables the GPS functionality on the modem.
 * 
 * @param modem Pointer to the esp_modem_dce_t structure representing the modem.
 * @return GPS_SUCCESS on success, error code otherwise.
 */
int gps_enable(esp_modem_dce_t *modem);

/**
 * @brief Disables the GPS functionality on the modem.
 * 
 * @param modem Pointer to the esp_modem_dce_t structure representing the modem.
 * @return GPS_SUCCESS on success, error code otherwise.
 */
int gps_disable(esp_modem_dce_t *modem);

/**
 * @brief Retrieves the current GPS mode from the modem.
 * 
 * @param status Pointer to gps_status_t structure to store the GPS status, you must allocate it before calling.
 * @param modem Pointer to the esp_modem_dce_t structure representing the modem.
 * @return GPS_SUCCESS on success, error code otherwise.
 */
int gps_get_mode(gps_status_t *status, esp_modem_dce_t *modem);

/**
 * @brief Retrieves the current GPS data from the modem.
 * 
 * @param data Pointer to gps_data_t structure to store the GPS data, you must allocate it before calling.
 * @param modem Pointer to the esp_modem_dce_t structure representing the modem.
 * @return GPS_SUCCESS on success, error code otherwise.
 */
int gps_get_data(gps_data_t *data, esp_modem_dce_t *modem);

/**
 * @brief Get a human-readable error message for a given error code.
 * 
 * @param code Error code.
 * @return Corresponding error message string.
 */
char *gps_get_error_message(int code);

#endif // GPS_H