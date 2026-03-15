#include "gps_encode.h"

#include "esp_log.h"

bool encode_string_callback(pb_ostream_t *stream, const pb_field_t *field, 
                            void * const *arg) {
    const char *str = (const char *)(*arg);
    
    if (!pb_encode_tag_for_field(stream, field)) {
        return false;
    }
    
    return pb_encode_string(stream, (const uint8_t *)str, strlen(str));
}

Hemisphere char_to_hemisphere(char c) {

    switch (c) {
        case 'N':
            return location_Hemisphere_NORTH;
        case 'S':
            return location_Hemisphere_SOUTH;
        case 'E':
            return location_Hemisphere_EAST;
        case 'W':
            return location_Hemisphere_WEST;
        default:
            return location_Hemisphere_UNDEFINED;
    }
}

uint8_t *gps_encode_location(gps_data_t *data, size_t *bytes_written) {

    Location location = location_Location_init_zero;

    ESP_LOGI("ENCODE", "Encoding GPS Data: Lat=%s %c, Lon=%s %c, Alt=%.2f, Speed=%.2f, Heading=%.2f\n",
          data->latitude, data->latHemisphere,
          data->longitude, data->lonHemisphere,
          data->altitude, data->speed, data->heading);

    location.latitude.funcs.encode = encode_string_callback;
    location.latitude.arg = (void *)data->latitude;

    location.longitude.funcs.encode = encode_string_callback;
    location.longitude.arg = (void *)data->longitude;

    location.latitude_hemisphere = char_to_hemisphere(data->latHemisphere);
    location.longitude_hemisphere = char_to_hemisphere(data->lonHemisphere);

    location.altitude = data->altitude;
    location.speed = data->speed;
    location.heading = data->heading;
    location.mode = data->mode;
    location.satellite_type = data->satelliteType;
    location.fix_status = data->fixStatus;
    location.satellites_used = data->satellitesUsed;
    location.time = data->timeUTC;
    location.date = data->date;
    location.hdop = data->hdop;
    location.pdop = data->pdop;
    location.vdop = data->vdop;

    size_t encoded_size;
    if (!pb_get_encoded_size(&encoded_size, location_Location_fields, &location)) {
        ESP_LOGE("ENCODE", "Failed to get encoded size");
        return NULL;
    }

    uint8_t *buffer = malloc(encoded_size);
    if (buffer == NULL) {
        ESP_LOGE("ENCODE", "Failed to allocate memory");
        return NULL;
    }

    pb_ostream_t stream = pb_ostream_from_buffer(buffer, encoded_size);

    if (!pb_encode(&stream, location_Location_fields, &location)) {
        ESP_LOGE("ENCODE", "Encoding failed: %s", PB_GET_ERROR(&stream));
        free(buffer);
        return NULL;
    }

    *bytes_written = stream.bytes_written;
    return buffer;
}