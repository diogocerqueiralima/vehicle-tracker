#ifndef GPS_H

#define GPS_H

#define GPS_AT_TIMEOUT_MS			2000
#define GPS_START_TIMEOUT_MS		5000
#define GPS_STOP_DELAY_MS			1000

// Bounds of the interval the module reports the location at, its command takes it in seconds and holds it in a single byte.
#define GPS_MIN_REPORT_INTERVAL_S	1
#define GPS_MAX_REPORT_INTERVAL_S	255

#include <stdint.h>
#include "esp_err.h"

/**
 * @brief Fix strategy used by the GNSS engine, matching the <mode> argument of AT+CGPS and the
 * values of the gps_mode BLE characteristic (docs/device/ble/gps/overview.md).
 */
typedef enum
{
    GPS_MODE_STANDALONE = 1,
    GPS_MODE_UE_BASED = 2,
    GPS_MODE_UE_ASSISTED = 3,
} gps_mode_t;

// Representation of each fix strategy in the gps_mode BLE characteristic (docs/device/ble/gps/overview.md).
static constexpr char MODE_STANDALONE[] = "standalone";
static constexpr char MODE_UE_BASED[] = "ue-based";
static constexpr char MODE_UE_ASSISTED[] = "ue-assisted";

/**
 * @brief Type of fix obtained from the satellite system, as reported in location data
 * (docs/device/data/location-data.md).
 */
typedef enum
{
    GPS_FIX_NONE = 0,
    GPS_FIX_2D = 1,
    GPS_FIX_3D = 2,
} gps_fix_type_t;

/**
 * @brief A single location sample read from the GNSS engine, holding the attributes the device
 * reports as location data (docs/device/data/location-data.md).
 */
typedef struct
{
    double latitude;            /**< Latitude in decimal degrees, negative in the southern hemisphere. */
    double longitude;           /**< Longitude in decimal degrees, negative in the western hemisphere. */
    double altitude;            /**< Altitude in meters above sea level, 0 without a 3D fix. */
    double speed;               /**< Speed over ground in knots. */
    double heading;             /**< Course over ground in degrees, 0 is North. */
    uint16_t satellite_count;   /**< Number of satellites used for the fix, across all constellations. */
    double hdop;                /**< Horizontal dilution of precision, lower is more accurate. */
    double vdop;                /**< Vertical dilution of precision, lower is more accurate, 0 without a 3D fix. */
    gps_fix_type_t fix_type;    /**< Type of fix the sample was obtained with. */
    int64_t timestamp;          /**< Satellite UTC time of the sample, in milliseconds since the epoch. */
} gps_location_t;

/**
 * @brief GPS behaviour as configured over BLE by the mobile application.
 */
typedef struct
{
    uint32_t update_interval;   /**< Seconds between location updates while the device is moving. */
    uint32_t timeout;           /**< Maximum seconds to wait for a fix before giving up. */
    gps_mode_t mode;            /**< Fix strategy the GNSS engine is started with. */
} gps_config_t;

/**
 *
 * @brief Converts a fix strategy to the string the mobile application writes to the gps_mode BLE
 * characteristic (docs/device/ble/gps/overview.md).
 *
 * @param mode The fix strategy to convert.
 * @return The null-terminated representation of the mode, or NULL if it is not a known mode.
 */
const char* gps_mode_to_string(gps_mode_t mode);

/**
 *
 * @brief Converts the string the mobile application writes to the gps_mode BLE characteristic
 * (docs/device/ble/gps/overview.md) to the fix strategy it stands for.
 *
 * @param text The null-terminated representation of the mode.
 * @param out_mode Variable that receives the fix strategy.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND if the string is not a known mode, or an appropriate
 * error code on failure.
 */
esp_err_t gps_mode_from_string(const char* text, gps_mode_t* out_mode);

/**
 *
 * @brief Loads the GPS configuration written by the mobile application over BLE. Storage must
 * already be initialized and the settings must have been seeded with their documented defaults,
 * which the BLE service does on first boot.
 *
 * @param out_config Structure that receives the configuration.
 * @return ESP_OK on success, ESP_ERR_NVS_NOT_FOUND if a setting was never written, or an
 * appropriate error code on failure.
 */
esp_err_t gps_load_config(gps_config_t* out_config);

/**
 *
 * @brief Starts the GNSS engine of the modem with the given fix strategy. The modem must already be
 * initialized with modem_init() and be in command mode. The module rejects a mode change while the
 * engine is on, so it must be stopped with gps_stop() before it can be started again.
 *
 * @param mode The fix strategy to start the GNSS engine with.
 * @return ESP_OK on success, ESP_ERR_INVALID_STATE if the GNSS engine is already running, or an
 * appropriate error code on failure.
 */
esp_err_t gps_start(gps_mode_t mode);

/**
 *
 * @brief Stops the GNSS engine of the modem, releasing the power it draws. Waits for the module to
 * settle so the engine can be started again right after. Only stops the engine, if there is no subscription active.
 *
 * @return ESP_OK on success, ESP_ERR_INVALID_STATE if a subscription is still active, or an
 * appropriate error code on failure.
 */
esp_err_t gps_stop();

/**
 *
 * @brief Checks whether the GNSS engine of the modem is currently running.
 *
 * @param out_running Variable that receives true if the GNSS engine is on, false otherwise.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t gps_is_running(bool* out_running);

/**
 * @brief Callback invoked with every location the GNSS engine reports while a subscription started
 * with gps_subscribe() is active.
 *
 * The callback runs in the task that reads the modem, so it must return quickly and must not issue
 * any AT command, which includes every other function of this module: doing so blocks the task that
 * would deliver the answer. Work that takes long belongs in a task of its own, handed the sample
 * from here.
 *
 * @param location The reported location, only valid for the duration of the call.
 * @param arg The opaque argument gps_subscribe() was given.
 */
typedef void (*gps_location_cb_t)(const gps_location_t* location, void* arg);

/**
 *
 * @brief Subscribes to the location of the GNSS engine, which the module then reports on its own
 * every interval until gps_unsubscribe() is called. The engine must have been started with gps_start(); only
 * the locations it acquired a fix for are reported, so nothing is delivered until it has one, which
 * takes from a few seconds to a few minutes after a cold start. A single subscription is supported.
 *
 * @param interval_seconds Seconds between two reported locations, from GPS_MIN_REPORT_INTERVAL_S to
 * GPS_MAX_REPORT_INTERVAL_S.
 * @param callback The callback every reported location is handed to.
 * @param arg Opaque argument passed back to the callback.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG if the interval is out of bounds or no callback was
 * given, ESP_ERR_INVALID_STATE if a subscription is already active, or an appropriate error code on
 * failure.
 */
esp_err_t gps_subscribe(uint32_t interval_seconds, gps_location_cb_t callback, void* arg);

/**
 *
 * @brief Cancels the subscription started with gps_subscribe(), stopping the reporting of the
 * location. The GNSS engine itself keeps running, so the location can be subscribed to again right
 * afterwards. A location being delivered while this function runs still reaches the callback.
 *
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t gps_unsubscribe();

#endif
