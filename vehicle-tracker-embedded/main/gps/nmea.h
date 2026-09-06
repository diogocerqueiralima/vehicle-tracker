#ifndef NMEA_H

#define NMEA_H

#include "esp_err.h"
#include "gps.h"

/**
 * @brief Sentence the location data is built from, one bit each so that the ones a burst is made of
 * are held together as the mask of the sentences it carries.
 */
typedef enum
{
    NMEA_SENTENCE_GGA = 1 << 0,
    NMEA_SENTENCE_RMC = 1 << 1,
    NMEA_SENTENCE_GSA = 1 << 2,
} nmea_sentence_t;

/**
 *
 * @brief Reports which sentence a line carries, reading its identifier alone: neither the fields it
 * holds nor the checksum they are sent with are validated, so a line this understands is not
 * necessarily one nmea_parse() accepts. It is meant for a caller that has to know which sentence
 * arrived before it is merged, a corrupted one included.
 *
 * @param sentence The null-terminated line to identify, starting with '$'.
 * @param out_sentence Variable that receives which sentence it carries, only written on success.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG if no line or no variable was given,
 * ESP_ERR_NOT_SUPPORTED if it is not one of the understood sentences, or ESP_ERR_INVALID_RESPONSE if
 * it carries no identifier at all.
 */
esp_err_t nmea_sentence_type(const char* sentence, nmea_sentence_t* out_sentence);

/**
 *
 * @brief Parses a single NMEA 0183 sentence and merges the attributes it carries into a location
 * sample. Only the sentences the location data is built from are understood: GGA (position,
 * altitude, satellites in use and horizontal dilution), RMC (position, speed, heading and satellite
 * UTC time) and GSA (fix type and dilutions), from any talker. A field the module left empty leaves
 * the attribute it maps to untouched, so a caller feeds the sentences of one burst to this function
 * over a sample it zeroed beforehand.
 *
 * @param sentence The null-terminated sentence to parse, starting with '$' and carrying its
 * checksum. A trailing carriage return and line feed are allowed.
 * @param out_location Structure the parsed attributes are merged into.
 * @param out_sentence Variable that receives which sentence was parsed, only written on success, or
 * NULL when the caller has no use for it.
 * @return ESP_OK on success, ESP_ERR_NOT_SUPPORTED if the sentence is not one of the understood
 * ones, ESP_ERR_INVALID_STATE if it is one of them but was sent without the fix its attributes are
 * only reported with, ESP_ERR_INVALID_CRC if its checksum does not match, ESP_ERR_INVALID_SIZE if it
 * is longer than the standard allows, or ESP_ERR_INVALID_RESPONSE if it is malformed.
 */
esp_err_t nmea_parse(const char* sentence, gps_location_t* out_location, nmea_sentence_t* out_sentence);

#endif
