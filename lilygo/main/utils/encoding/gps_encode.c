#include "gps_encode.h"

#include "esp_log.h"

bool string_callback(pb_ostream_t *stream, const pb_field_t *field, void * const *arg) {
    const char *str = *(const char**)arg;
    size_t len = strlen(str);
    return pb_encode_string(stream, (const pb_byte_t*)str, len);
}

uint8_t *gps_encode_location(gps_data_t *data, size_t *bytes_written) {

    Location location = location_Location_init_zero;

    location.date.funcs.encode = &string_callback;
    location.date.arg = &data->date;

    location.time.funcs.encode = &string_callback;
    location.time.arg = &data->timeUTC;

    location.latitude.funcs.encode = &string_callback;
    location.latitude.arg = &data->latitude;

    location.longitude.funcs.encode = &string_callback;
    location.longitude.arg = &data->longitude;

    char lat_hemi_str[2] = {data->latHemisphere, '\0'};
    char lon_hemi_str[2] = {data->lonHemisphere, '\0'};

    location.latitude_hemisphere.funcs.encode = &string_callback;
    location.latitude_hemisphere.arg = (void *) &lat_hemi_str;

    location.longitude_hemisphere.funcs.encode = &string_callback;
    location.longitude_hemisphere.arg = (void *) &lon_hemi_str;

    location.altitude = data->altitude;
    location.speed = data->speed;
    location.heading = data->heading;

    location.mode = data->mode;
    location.satellite_type = data->satelliteType;
    location.fix_status = data->fixStatus;
    location.satellites_used = data->satellitesUsed;
    location.hdop = data->hdop;
    location.pdop = data->pdop;
    location.vdop = data->vdop;
    location.raw_date = data->date;
    location.time_utc = data->timeUTC;

    size_t encoded_size;
    if (!pb_get_encoded_size(&encoded_size, schemas_v1_location_Location_fields, &location)) {
        ESP_LOGE("ENCODE", "Failed to get encoded size");
        return NULL;
    }

    uint8_t *buffer = malloc(encoded_size);
    
    if (buffer == NULL) {
        return NULL;
    }

    pb_ostream_t stream = pb_ostream_from_buffer(buffer, encoded_size);

    if (!pb_encode(&stream, schemas_v1_location_Location_fields, &location)) {
        ESP_LOGE("ENCODE", "Encoding failed: %s", PB_GET_ERROR(&stream));
        free(buffer);
        return NULL;
    }

    *bytes_written = stream.bytes_written;
    return buffer;
}