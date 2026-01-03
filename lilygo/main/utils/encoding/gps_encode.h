#ifndef GPS_ENCODE_H
#define GPS_ENCODE_H

#include <stdbool.h>

#include "pb_encode.h"
#include "gps/gps.h"
#include "generated/vehicle-tracker-schemas/Location.pb.h"

typedef location_Location   Location;
typedef location_Hemisphere Hemisphere;

/**
 * 
 * @brief   Callback function to encode gps data into protobuf.
 *          You should pass a pointer to receive the quantity of bytes written.
 *          You should free the returned buffer after use.
 * 
 * @param data Pointer to gps_data_t structure containing GPS data.
 * @param bytes_written Pointer to size_t variable to store the number of bytes written.
 * @return uint8_t* Pointer to the encoded protobuf data buffer. NULL if encoding fails.
 */
uint8_t *gps_encode_location(gps_data_t *data, size_t *bytes_written);

#endif // GPS_ENCODE_H