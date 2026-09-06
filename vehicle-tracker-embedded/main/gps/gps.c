#include "gps.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "ble/services/gps_service.h"
#include "modem/modem.h"
#include "nmea.h"
#include "storage/storage.h"

static const char* LOG_TAG = "gps";

// Commands that turn the GNSS engine on and off, the argument of the first one is the fix strategy to start it with.
static constexpr char START_COMMAND[] = "AT+CGPS=1,%d";
static constexpr char STOP_COMMAND[] = "AT+CGPS=0";

// Command that reports the state of the GNSS engine, answered with "<prefix> <on_off>,<mode>".
static constexpr char STATUS_COMMAND[] = "AT+CGPS?";
static constexpr char STATUS_RESPONSE_PREFIX[] = "+CGPS:";

// Commands that start and stop the reporting of the NMEA sentences the location data is built from,
// which the module then sends on its own in a burst every <time> seconds, the first argument. The
// sentences of a burst are chosen by a mask, one bit each in the order GGA, RMC, GSV, GSA and VTG,
// so 11 asks for GGA, RMC and GSA.
static constexpr char REPORT_COMMAND[] = "AT+CGPSINFOCFG=%lu,11";
static constexpr char STOP_REPORT_COMMAND[] = "AT+CGPSINFOCFG=0,11";

// Sentences a burst is made of, one for every bit of the mask above. The location a burst carries is
// only whole once every one of them was merged into it.
static constexpr unsigned BURST_SENTENCES = NMEA_SENTENCE_GGA | NMEA_SENTENCE_RMC | NMEA_SENTENCE_GSA;

// Subscriber the bursts are reported to and the argument it is invoked with, set by gps_subscribe()
// and cleared by gps_unsubscribe(). Both are written by the task that subscribes and read by the
// task that reads the modem, which is the one that runs the listener.
static gps_location_cb_t location_callback = nullptr;
static void* location_callback_arg = nullptr;

const char* gps_mode_to_string(const gps_mode_t mode)
{
    switch (mode)
    {
    case GPS_MODE_STANDALONE:
        return MODE_STANDALONE;
    case GPS_MODE_UE_BASED:
        return MODE_UE_BASED;
    case GPS_MODE_UE_ASSISTED:
        return MODE_UE_ASSISTED;
    default:
        return nullptr;
    }
}

esp_err_t gps_mode_from_string(const char* text, gps_mode_t* out_mode)
{
    if (text == nullptr || out_mode == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // Compare against the representation of every known mode, the enumeration is contiguous.
    for (gps_mode_t mode = GPS_MODE_STANDALONE; mode <= GPS_MODE_UE_ASSISTED; mode++)
    {
        if (strcmp(text, gps_mode_to_string(mode)) == 0)
        {
            *out_mode = mode;
            return ESP_OK;
        }
    }

    return ESP_ERR_NOT_FOUND;
}

/**
 *
 * Loads the fix strategy the GNSS engine is started with from storage.
 *
 * @param out_mode Pointer to a variable that receives the fix strategy read from storage.
 * @return ESP_OK on success, ESP_ERR_NVS_NOT_FOUND if the value was never written, or an appropriate error
 */
static esp_err_t load_mode(gps_mode_t* out_mode)
{
    // 1. Get the size of the stored value to determine how much data to read.
    size_t len = 0;
    const esp_err_t size_err = get_data_size(GPS_MODE_NAMESPACE, &len);
    if (size_err != ESP_OK)
    {
        return size_err;
    }

    // 2. No accepted value is empty or longer than the buffer, so such a value cannot be a mode.
    if (len == 0)
    {
        return ESP_ERR_INVALID_SIZE;
    }

    // 3. Read the stored value. It is written without a terminator, so terminate it before comparing.
    char buf[len + 1];
    const esp_err_t err = load_data(GPS_MODE_NAMESPACE, buf, len);
    if (err != ESP_OK)
    {
        return err;
    }
    buf[len] = '\0';

    return gps_mode_from_string(buf, out_mode);
}

esp_err_t gps_load_config(gps_config_t* out_config)
{
    if (out_config == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 1. Read the interval between location updates, in seconds.
    uint32_t update_interval = 0;
    esp_err_t err = load_uint32(GPS_UPDATE_INTERVAL_NAMESPACE, &update_interval);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to load GPS update interval: %s", esp_err_to_name(err));
        return err;
    }

    // 2. Read the maximum time to wait for a fix, in seconds.
    uint32_t timeout = 0;
    err = load_uint32(GPS_TIMEOUT_NAMESPACE, &timeout);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to load GPS timeout: %s", esp_err_to_name(err));
        return err;
    }

    // 3. Read the fix strategy the GNSS engine is started with.
    gps_mode_t mode;
    err = load_mode(&mode);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to load GPS mode: %s", esp_err_to_name(err));
        return err;
    }

    // 4. Only publish the configuration once every setting was read successfully.
    out_config->update_interval = update_interval;
    out_config->timeout = timeout;
    out_config->mode = mode;

    ESP_LOGI(LOG_TAG, "Loaded GPS configuration: update interval %lus, timeout %lus, mode %s",
             (unsigned long)update_interval, (unsigned long)timeout, gps_mode_to_string(mode));

    return ESP_OK;
}

esp_err_t gps_start(const gps_mode_t mode)
{
    // 1. The enumeration mirrors the <mode> argument of AT+CGPS, so anything outside it is rejected
    // before it reaches the module.
    if (gps_mode_to_string(mode) == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. The module rejects a mode change while the engine is on, so a running engine is an error
    // instead of a silent restart in a mode the caller did not ask for.
    bool running = false;
    esp_err_t err = gps_is_running(&running);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to check whether the GNSS engine is running: %s", esp_err_to_name(err));
        return err;
    }

    if (running)
    {
        ESP_LOGI(LOG_TAG, "GNSS engine is already running");
        return ESP_OK;
    }

    // 3. Start the engine with the requested fix strategy.
    char cmd[AT_COMMAND_MAX_LEN];
    snprintf(cmd, sizeof(cmd), START_COMMAND, mode);

    char response[ESP_MODEM_C_API_STR_BUF_SIZE];
    err = modem_at(cmd, response, GPS_START_TIMEOUT_MS);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to start the GNSS engine: %s", esp_err_to_name(err));
        return err;
    }

    ESP_LOGI(LOG_TAG, "GNSS engine started in %s mode", gps_mode_to_string(mode));

    return ESP_OK;
}

esp_err_t gps_stop()
{
    // 1. A subscriber is handed the locations of an engine that is on, so stopping the engine under
    // it would leave it waiting for locations that are never reported. The subscription is cancelled
    // with gps_unsubscribe() first.
    if (location_callback != nullptr)
    {
        ESP_LOGE(LOG_TAG, "A subscription to the location is still active");
        return ESP_ERR_INVALID_STATE;
    }

    // 2. Turn the engine off, releasing the power it draws.
    const esp_err_t err = modem_at(STOP_COMMAND, nullptr, GPS_AT_TIMEOUT_MS);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to stop the GNSS engine: %s", esp_err_to_name(err));
        return err;
    }

    ESP_LOGI(LOG_TAG, "GNSS engine stopped");

    return ESP_OK;
}

esp_err_t gps_is_running(bool* out_running)
{
    if (out_running == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 1. Ask the module for the state of its GNSS engine.
    char response[ESP_MODEM_C_API_STR_BUF_SIZE];
    const esp_err_t err = modem_at(STATUS_COMMAND, response, GPS_AT_TIMEOUT_MS);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to read the state of the GNSS engine: %s", esp_err_to_name(err));
        return err;
    }

    // 2. The answer carries the state alongside the mode the engine runs in, only the state is needed.
    const char* state = strstr(response, STATUS_RESPONSE_PREFIX);
    if (state == nullptr)
    {
        ESP_LOGE(LOG_TAG, "Unexpected answer to %s: %s", STATUS_COMMAND, response);
        return ESP_ERR_INVALID_RESPONSE;
    }

    // 3. The engine is on when the state is anything other than zero.
    *out_running = strtol(state + strlen(STATUS_RESPONSE_PREFIX), nullptr, 10) != 0;

    return ESP_OK;
}

/**
 *
 * @brief Collects the sentences of the bursts the module reports while a subscription is active, handing
 * the location they carry to the subscriber once a burst is whole. Every line the module reports on
 * its own reaches this listener, the ones that are not sentences are not part of a burst.
 *
 * @param line the line the module reported, without its terminator
 * @param arg unused, the subscriber the bursts are reported to is the one of this module
 */
static void on_report(const char* line, void* arg)
{
    // 1. The sentences of a burst are merged into this location, which is only whole once every one of them was merged into it.
    static gps_location_t burst = {};
    static unsigned burst_sentences = 0;

    // 2. Nothing to report a burst to, drop the one being collected so that a later subscription
    // starts on a burst of its own instead of on the sentences left over from this one.
    const gps_location_cb_t callback = location_callback;
    if (callback == nullptr)
    {
        burst = (gps_location_t){};
        burst_sentences = 0;
        return;
    }

    // 3. Read which sentence the line carries, anything else the module reports, such as the answer
    // to a command, is no part of a burst.
    nmea_sentence_t sentence = 0;
    if (nmea_sentence_type(line, &sentence) != ESP_OK)
    {
        return;
    }

    // 4. A sentence the burst being collected already carries belongs to the next one: the burst it
    // was collected for lost a sentence, dropped on its way or rejected, so it is never whole and is
    // never reported. Merging this sentence into it anyway would hand its leftovers to the burst that
    // does complete and leave every burst after it one sentence out of step, so this sentence starts
    // a burst of its own instead.
    if ((burst_sentences & sentence) != 0)
    {
        burst = (gps_location_t){};
        burst_sentences = 0;
    }

    // 5. Merge the sentence into the burst, each of them carries a part of the location. One that
    // could not be read carries none of it, so the burst is still missing it.
    if (nmea_parse(line, &burst, nullptr) != ESP_OK)
    {
        return;
    }

    // 6. Wait for the rest of the burst, the location it carries is only whole once every sentence was merged into it.
    burst_sentences |= sentence;
    if (burst_sentences != BURST_SENTENCES)
    {
        return;
    }

    // 7. The burst is whole, the sentences of the next one are merged into a burst of their own.
    const gps_location_t location = burst;

    burst = (gps_location_t){};
    burst_sentences = 0;

    // 8. The module reports a burst every interval whether or not it has a fix, leaving the
    // attributes it has none for empty. Such a burst carries no location to report.
    if (location.fix_type == GPS_FIX_NONE)
    {
        return;
    }

    // 9. Hand the location to the subscriber.
    callback(&location, location_callback_arg);
}

// Listener the sentences the module reports reach this module through, registered while a subscription is active.
static modem_urc_listener_t report_listener = {.callback = on_report};

esp_err_t gps_subscribe(const uint32_t interval_seconds, const gps_location_cb_t callback, void* arg)
{

    // 1. The interval is in seconds, the module takes a value from 1 to 255, and a callback is required.
    if (callback == nullptr || interval_seconds < GPS_MIN_REPORT_INTERVAL_S || interval_seconds > GPS_MAX_REPORT_INTERVAL_S)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. The module reports the bursts to whoever asked for them, so a second subscription would
    // take the bursts of the first one over instead of being reported alongside it.
    if (location_callback != nullptr)
    {
        ESP_LOGE(LOG_TAG, "A subscription to the location is already active");
        return ESP_ERR_INVALID_STATE;
    }

    // 3. Publish the subscriber before the module is asked to report, the listener is handed the
    // sentences of the first burst as soon as they arrive.
    location_callback = callback;
    location_callback_arg = arg;

    esp_err_t err = modem_add_urc_listener(&report_listener);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to listen to what the module reports: %s", esp_err_to_name(err));
        location_callback = nullptr;
        return err;
    }

    // 4. Ask the module for a burst every interval, which it keeps reporting until it is stopped, so
    // the command is only issued this once.
    char cmd[AT_COMMAND_MAX_LEN];
    snprintf(cmd, sizeof(cmd), REPORT_COMMAND, (unsigned long)interval_seconds);

    char response[ESP_MODEM_C_API_STR_BUF_SIZE] = "";
    err = modem_at(cmd, response, GPS_AT_TIMEOUT_MS);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to start reporting the location: %s, module answered: %s",
                 esp_err_to_name(err), response);

        modem_remove_urc_listener(&report_listener);
        location_callback = nullptr;
        return err;
    }

    ESP_LOGI(LOG_TAG, "Subscribed to the location, reported every %lus", (unsigned long)interval_seconds);

    return ESP_OK;
}

esp_err_t gps_unsubscribe()
{

    // 1. Drop the subscriber before the reporting is stopped, so the bursts that are still on their
    // way are no longer delivered to it.
    location_callback = nullptr;
    location_callback_arg = nullptr;

    // 2. Stop the reporting, the GNSS engine itself keeps running.
    const esp_err_t err = modem_at(STOP_REPORT_COMMAND, nullptr, GPS_AT_TIMEOUT_MS);
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to stop reporting the location: %s", esp_err_to_name(err));
    }

    // 3. Stop listening to what the module reports whether or not the reporting stopped, the
    // listener has no subscriber to hand a burst to either way.
    const esp_err_t listener_err = modem_remove_urc_listener(&report_listener);
    if (listener_err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to stop listening to what the module reports: %s",
                 esp_err_to_name(listener_err));
    }

    if (err != ESP_OK)
    {
        return err;
    }

    ESP_LOGI(LOG_TAG, "Unsubscribed from the location");

    return listener_err;
}
