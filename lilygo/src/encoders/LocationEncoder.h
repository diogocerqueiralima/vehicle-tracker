#include "vehicle-tracker-schemas/Location.pb.h"
#include "microcontroller/GPS.h"
#include "pb_encode.h"
#include <string.h>

bool string_encode_callback(pb_ostream_t *stream, const pb_field_t *field, void *const *arg) {

    const char *str = (const char *)(*arg);
    
    if (!pb_encode_tag_for_field(stream, field))
        return false;
    
    return pb_encode_string(stream, (const uint8_t *)str, strlen(str));
}

bool encode_location(const GPSInfo &gps, pb_ostream_t stream, size_t *encoded_size) {

    schemas_v1_location_Location msg = schemas_v1_location_Location_init_zero;

    msg.altitude = gps.altitude;
    msg.speed = gps.speed;
    msg.heading = gps.course;

    static char lat_str[16];
    static char lon_str[16];
    static char time_str[16];
    static char date_str[16];
    static char ns_str[2];
    static char ew_str[2];

    strncpy(time_str, gps.utcTime.c_str(), sizeof(time_str)-1);
    time_str[sizeof(time_str)-1] = '\0';

    strncpy(date_str, gps.date.c_str(), sizeof(date_str)-1);
    date_str[sizeof(date_str)-1] = '\0';

    strncpy(lat_str, gps.lat.c_str(), sizeof(lat_str)-1);
    lat_str[sizeof(lat_str)-1] = '\0';

    strncpy(lon_str, gps.lon.c_str(), sizeof(lon_str)-1);
    lon_str[sizeof(lon_str)-1] = '\0';

    ns_str[0] = gps.NS;
    ns_str[1] = '\0';
    ew_str[0] = gps.EW;
    ew_str[1] = '\0';

    msg.latitude.funcs.encode = &string_encode_callback;
    msg.latitude.arg = (void*)lat_str;

    msg.longitude.funcs.encode = &string_encode_callback;
    msg.longitude.arg = (void*)lon_str;

    msg.time.funcs.encode = &string_encode_callback;
    msg.time.arg = (void*)time_str;

    msg.date.funcs.encode = &string_encode_callback;
    msg.date.arg = (void*)date_str;

    msg.latitude_hemisphere.funcs.encode = &string_encode_callback;
    msg.latitude_hemisphere.arg = (void*)ns_str;

    msg.longitude_hemisphere.funcs.encode = &string_encode_callback;
    msg.longitude_hemisphere.arg = (void*)ew_str;

    if (!pb_encode(&stream, schemas_v1_location_Location_fields, &msg)) {
        Serial.print("Encode failed: ");
        Serial.println(PB_GET_ERROR(&stream));
        return false;
    }

    *encoded_size = stream.bytes_written;
    return true;
}
