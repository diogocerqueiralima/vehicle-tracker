#include "nmea.h"

#include <ctype.h>
#include <stdlib.h>
#include <string.h>

// Longest sentence the standard allows, the leading '$' and the trailing carriage return and line
// feed included.
static constexpr size_t MAX_SENTENCE_LEN = 82;

// Most fields a understood sentence has, GSA carries an identifier, two modes, twelve satellites and
// three dilutions.
static constexpr size_t MAX_FIELDS = 20;

// A sentence is identified by a talker followed by the type of the sentence, only the type matters.
static constexpr size_t TALKER_LEN = 2;
static constexpr char TYPE_GGA[] = "GGA";
static constexpr char TYPE_RMC[] = "RMC";
static constexpr char TYPE_GSA[] = "GSA";

// The checksum of a sentence is written after '*' as two hexadecimal digits.
static constexpr char CHECKSUM_SEPARATOR = '*';
static constexpr size_t CHECKSUM_LEN = 2;

// Fields of a GGA sentence, counted from its identifier.
static constexpr size_t GGA_LATITUDE = 2;
static constexpr size_t GGA_NORTH_SOUTH = 3;
static constexpr size_t GGA_LONGITUDE = 4;
static constexpr size_t GGA_EAST_WEST = 5;
static constexpr size_t GGA_SATELLITES = 7;
static constexpr size_t GGA_HDOP = 8;
static constexpr size_t GGA_ALTITUDE = 9;
static constexpr size_t GGA_FIELDS = 10;

// Fields of an RMC sentence, counted from its identifier.
static constexpr size_t RMC_TIME = 1;
static constexpr size_t RMC_STATUS = 2;
static constexpr size_t RMC_LATITUDE = 3;
static constexpr size_t RMC_NORTH_SOUTH = 4;
static constexpr size_t RMC_LONGITUDE = 5;
static constexpr size_t RMC_EAST_WEST = 6;
static constexpr size_t RMC_SPEED = 7;
static constexpr size_t RMC_HEADING = 8;
static constexpr size_t RMC_DATE = 9;
static constexpr size_t RMC_FIELDS = 10;

// Fields of a GSA sentence, counted from its identifier.
static constexpr size_t GSA_FIX_TYPE = 2;
static constexpr size_t GSA_HDOP = 16;
static constexpr size_t GSA_VDOP = 17;
static constexpr size_t GSA_FIELDS = 18;

// The data of an RMC sentence is only valid while its status field reports it.
static constexpr char RMC_STATUS_VALID = 'A';

// Fix types of a GSA sentence, the ones the location data reports are named by gps_fix_type_t.
static constexpr long GSA_FIX_2D = 2;
static constexpr long GSA_FIX_3D = 3;

// Coordinates are written as degrees and minutes glued together, the hemisphere sits in the next
// field. Latitudes use two digits of degrees, longitudes three.
static constexpr size_t LATITUDE_DEGREE_DIGITS = 2;
static constexpr size_t LONGITUDE_DEGREE_DIGITS = 3;
static constexpr char SOUTH = 'S';
static constexpr char WEST = 'W';
static constexpr double MINUTES_PER_DEGREE = 60.0;

// Dates are written as "ddmmyy" and times as "hhmmss.ss", both with a fixed number of digits.
static constexpr size_t DATE_LEN = 6;
static constexpr size_t TIME_LEN = 6;
static constexpr size_t DATE_TIME_FIELD_DIGITS = 2;

// A two digit year below this one belongs to the current century, the standard counts from 1980.
static constexpr int YEAR_CENTURY_PIVOT = 80;

static constexpr int64_t MS_PER_SECOND = 1000;
static constexpr int64_t SECONDS_PER_MINUTE = 60;
static constexpr int64_t SECONDS_PER_HOUR = 3600;
static constexpr int64_t SECONDS_PER_DAY = 86400;

/**
 *
 * Parses a field holding a floating point number, returning false when it is empty or not a number.
 *
 * @param field the null-terminated field to parse, which may be empty
 * @param out_value pointer to a variable that receives the parsed value
 * @return true if the field was successfully parsed, false otherwise
 */
static bool parse_double(const char* field, double* out_value)
{
    if (field == nullptr || field[0] == '\0')
    {
        return false;
    }

    char* end = nullptr;
    const double value = strtod(field, &end);
    if (end == field)
    {
        return false;
    }

    *out_value = value;
    return true;
}

/**
 *
 * Parses a field holding a long integer, returning false when it is empty or not a number.
 *
 * @param field the null-terminated field to parse, which may be empty
 * @param out_value pointer to a variable that receives the parsed value
 * @return true if the field was successfully parsed, false otherwise
 */
static bool parse_long(const char* field, long* out_value)
{
    if (field == nullptr || field[0] == '\0')
    {
        return false;
    }

    char* end = nullptr;
    const long value = strtol(field, &end, 10);
    if (end == field)
    {
        return false;
    }

    *out_value = value;
    return true;
}

/**
 *
 * Parses a field holding a decimal integer, returning -1 when it is empty or not a number.
 *
 * @param text the null-terminated field to parse, which may be empty
 * @param count the number of digits to parse from the field
 * @return the parsed value, or -1 if the field was empty or not a number
 */
static int parse_digits(const char* text, const size_t count)
{
    int value = 0;

    for (size_t i = 0; i < count; i++)
    {
        if (!isdigit((unsigned char)text[i]))
        {
            return -1;
        }

        value = value * 10 + (text[i] - '0');
    }

    return value;
}

/**
 *
 * Parses a coordinate from a field holding its degrees and minutes, and a field holding its hemisphere.
 *
 * @param field the null-terminated field holding the degrees and minutes, which may be empty
 * @param hemisphere the null-terminated field holding the hemisphere, which may be empty
 * @param degree_digits the number of digits in the degrees part of the field
 * @param out_value the pointer to a variable that receives the parsed value, which is always positive and only the
 * @return true if the coordinate was successfully parsed, false otherwise
 */
static bool parse_coordinate(const char* field, const char* hemisphere, const size_t degree_digits, double* out_value)
{
    if (field == nullptr || hemisphere == nullptr || strlen(field) <= degree_digits || hemisphere[0] == '\0')
    {
        return false;
    }

    // 1. The degrees are the leading digits of the field, the minutes are everything after them.
    const int degrees = parse_digits(field, degree_digits);
    if (degrees < 0)
    {
        return false;
    }

    double minutes = 0;
    if (!parse_double(field + degree_digits, &minutes))
    {
        return false;
    }

    // 2. The hemisphere only decides the sign, the value itself is always positive.
    double value = degrees + minutes / MINUTES_PER_DEGREE;
    if (hemisphere[0] == SOUTH || hemisphere[0] == WEST)
    {
        value = -value;
    }

    *out_value = value;
    return true;
}

/**
 *
 * translates a date in the civil calendar to the number of days since the epoch, which is 1970-01-01.
 *
 * @param year the year of the date, in the civil calendar
 * @param month the month of the date, in the civil calendar, 1-12
 * @param day the day of the date, in the civil calendar, 1-31
 * @return the number of days since the epoch
 */
static int64_t days_from_civil(int year, const int month, const int day)
{
    // The algorithm counts from March, so January and February belong to the previous year.
    year -= month <= 2;

    const int era = (year >= 0 ? year : year - 399) / 400;
    const int year_of_era = year - era * 400;
    const int day_of_year = (153 * (month + (month > 2 ? -3 : 9)) + 2) / 5 + day - 1;
    const int day_of_era = year_of_era * 365 + year_of_era / 4 - year_of_era / 100 + day_of_year;

    // 719468 is the number of days between the start of the first era and the epoch.
    return (int64_t)era * 146097 + day_of_era - 719468;
}

/**
 *
 * Parses a timestamp from a date and a time field, both in the format the module reports, and returns it
 *
 * @param date the null-terminated field holding the date, which may be empty
 * @param time the null-terminated field holding the time, which may be empty
 * @param out_timestamp the pointer to a variable that receives the parsed timestamp, in milliseconds since the epoch
 * @return true if the timestamp was successfully parsed, false otherwise
 */
static bool parse_timestamp(const char* date, const char* time, int64_t* out_timestamp)
{
    if (date == nullptr || time == nullptr || strlen(date) < DATE_LEN || strlen(time) < TIME_LEN)
    {
        return false;
    }

    // 1. Read the date, whose year only carries its last two digits.
    const int day = parse_digits(date, DATE_TIME_FIELD_DIGITS);
    const int month = parse_digits(date + DATE_TIME_FIELD_DIGITS, DATE_TIME_FIELD_DIGITS);
    int year = parse_digits(date + 2 * DATE_TIME_FIELD_DIGITS, DATE_TIME_FIELD_DIGITS);

    if (day < 0 || month < 0 || year < 0)
    {
        return false;
    }

    year += year < YEAR_CENTURY_PIVOT ? 2000 : 1900;

    // 2. Read the time, whose seconds carry the fraction the module reports.
    const int hour = parse_digits(time, DATE_TIME_FIELD_DIGITS);
    const int minute = parse_digits(time + DATE_TIME_FIELD_DIGITS, DATE_TIME_FIELD_DIGITS);

    double seconds = 0;
    if (hour < 0 || minute < 0 || !parse_double(time + 2 * DATE_TIME_FIELD_DIGITS, &seconds))
    {
        return false;
    }

    // 3. Count the milliseconds from the epoch to that date and time, both are UTC.
    const int64_t days = days_from_civil(year, month, day);
    const int64_t seconds_of_day = hour * SECONDS_PER_HOUR + minute * SECONDS_PER_MINUTE;

    *out_timestamp = (days * SECONDS_PER_DAY + seconds_of_day) * MS_PER_SECOND + (int64_t)(seconds * MS_PER_SECOND);
    return true;
}

/**
 *
 * Parses a GGA sentence, which reports the position, the altitude, the number of satellites in use and
 *
 * @param fields the array of null-terminated fields of the sentence, starting with its identifier
 * @param count the number of fields in the array
 * @param out_location the pointer to a structure that receives the parsed attributes, which are merged into it
 * @return true if the sentence was successfully parsed, false otherwise
 */
static esp_err_t parse_gga(char* fields[], const size_t count, gps_location_t* out_location)
{
    if (count < GGA_FIELDS)
    {
        return ESP_ERR_INVALID_RESPONSE;
    }

    parse_coordinate(fields[GGA_LATITUDE], fields[GGA_NORTH_SOUTH], LATITUDE_DEGREE_DIGITS,
                     &out_location->latitude);
    parse_coordinate(fields[GGA_LONGITUDE], fields[GGA_EAST_WEST], LONGITUDE_DEGREE_DIGITS,
                     &out_location->longitude);
    parse_double(fields[GGA_ALTITUDE], &out_location->altitude);
    parse_double(fields[GGA_HDOP], &out_location->hdop);

    long satellites = 0;
    if (parse_long(fields[GGA_SATELLITES], &satellites) && satellites >= 0)
    {
        out_location->satellite_count = (uint16_t)satellites;
    }

    return ESP_OK;
}

/**
 *
 * Parses an RMC sentence, which reports the position, the speed, the heading and the satellite UTC
 *
 * @param fields the array of null-terminated fields of the sentence, starting with its identifier
 * @param count the number of fields in the array
 * @param out_location the pointer to a structure that receives the parsed attributes, which are merged into it
 * @return true if the sentence was successfully parsed, false otherwise
 */
static esp_err_t parse_rmc(char* fields[], const size_t count, gps_location_t* out_location)
{
    if (count < RMC_FIELDS)
    {
        return ESP_ERR_INVALID_RESPONSE;
    }

    // The sentence is also sent without a fix, and everything it carries is meaningless then.
    if (fields[RMC_STATUS][0] != RMC_STATUS_VALID)
    {
        return ESP_ERR_INVALID_STATE;
    }

    parse_coordinate(fields[RMC_LATITUDE], fields[RMC_NORTH_SOUTH], LATITUDE_DEGREE_DIGITS,
                     &out_location->latitude);
    parse_coordinate(fields[RMC_LONGITUDE], fields[RMC_EAST_WEST], LONGITUDE_DEGREE_DIGITS,
                     &out_location->longitude);
    parse_double(fields[RMC_SPEED], &out_location->speed);
    parse_double(fields[RMC_HEADING], &out_location->heading);

    // The date is read out of what the satellites broadcast, which takes longer than the fix does, so
    // the sentence reports one it cannot date yet. An empty date leaves the sample at the epoch, which
    // reads as the time it was taken at rather than as the missing one it is.
    if (!parse_timestamp(fields[RMC_DATE], fields[RMC_TIME], &out_location->timestamp))
    {
        return ESP_ERR_INVALID_STATE;
    }

    return ESP_OK;
}

/**
 *
 * Parses a GSA sentence, which reports the fix type and the dilutions of precision.
 *
 * @param fields the array of null-terminated fields of the sentence, starting with its identifier
 * @param count the number of fields in the array
 * @param out_location the pointer to a structure that receives the parsed attributes, which are merged into it
 * @return true if the sentence was successfully parsed, false otherwise
 */
static esp_err_t parse_gsa(char* fields[], const size_t count, gps_location_t* out_location)
{
    if (count < GSA_FIELDS)
    {
        return ESP_ERR_INVALID_RESPONSE;
    }

    long fix_type = 0;
    if (parse_long(fields[GSA_FIX_TYPE], &fix_type))
    {
        if (fix_type == GSA_FIX_2D)
        {
            out_location->fix_type = GPS_FIX_2D;
        }
        else if (fix_type == GSA_FIX_3D)
        {
            out_location->fix_type = GPS_FIX_3D;
        }
        else
        {
            out_location->fix_type = GPS_FIX_NONE;
        }
    }

    parse_double(fields[GSA_HDOP], &out_location->hdop);
    parse_double(fields[GSA_VDOP], &out_location->vdop);

    return ESP_OK;
}

/**
 *
 * Extracts the body of a sentence, which sits between the leading '$' and the trailing checksum, and
 * validates the checksum. The body is copied to the provided buffer, which must be large enough to
 * hold it and a null terminator. The checksum is the exclusive or of every character of the body,
 * written as two hexadecimal digits after a '*'.
 *
 * @param sentence the null-terminated sentence to extract the body from, starting with '$' and carrying its checksum
 * @param body the buffer to copy the body to, which must be large enough to hold it and a null terminator
 * @param body_size the size of the body buffer, in bytes
 * @return the error code of the operation, ESP_OK on success, ESP_ERR_INVALID_RESPONSE if the sentence is malformed, ESP_ERR_INVALID_CRC if the checksum does not match, or ESP_ERR_INVALID_SIZE if the body is larger than the buffer
 */
static esp_err_t extract_body(const char* sentence, char* body, const size_t body_size)
{
    // 1. Every sentence starts with '$' and carries its checksum at the end.
    if (sentence[0] != '$')
    {
        return ESP_ERR_INVALID_RESPONSE;
    }

    const char* separator = strchr(sentence, CHECKSUM_SEPARATOR);
    if (separator == nullptr || strlen(separator + 1) < CHECKSUM_LEN)
    {
        return ESP_ERR_INVALID_RESPONSE;
    }

    // 2. The body sits between both, an empty one carries no identifier and no fields.
    const size_t len = separator - sentence - 1;
    if (len == 0)
    {
        return ESP_ERR_INVALID_RESPONSE;
    }

    if (len >= body_size)
    {
        return ESP_ERR_INVALID_SIZE;
    }

    // 3. The checksum is the exclusive or of every character of the body, written as two
    // hexadecimal digits.
    uint8_t checksum = 0;
    for (size_t i = 0; i < len; i++)
    {
        checksum ^= (uint8_t)sentence[i + 1];
    }

    const char expected[CHECKSUM_LEN + 1] = {separator[1], separator[2], '\0'};
    char* end = nullptr;
    const long value = strtol(expected, &end, 16);

    if (end != expected + CHECKSUM_LEN || value != checksum)
    {
        return ESP_ERR_INVALID_CRC;
    }

    memcpy(body, sentence + 1, len);
    body[len] = '\0';

    return ESP_OK;
}

/**
 *
 * Splits the body of a sentence into its fields, which are separated by commas. The first field is the
 * identifier of the sentence, the rest are its attributes. The fields are returned as an array
 * of pointers to the null-terminated fields, which are all stored in the body buffer. The array must
 * be large enough to hold the maximum number of fields the sentence can have, the rest is
 * dropped. The number of fields found is returned.
 *
 * @param body the null-terminated body of the sentence to split, which is modified in place
 * @param fields the array of pointers to the null-terminated fields, which must be large enough to hold the maximum number of fields
 * @param max_fields the maximum number of fields the array can hold
 * @return the number of fields found, which is at least 1 (the identifier) and at most max_fields
 */
static size_t split_fields(char* body, char* fields[], const size_t max_fields)
{
    size_t count = 1;
    fields[0] = body;

    for (char* character = body; *character != '\0'; character++)
    {
        if (*character != ',')
        {
            continue;
        }

        *character = '\0';

        // The sentences the parser understands are shorter than the array, the rest is dropped.
        if (count == max_fields)
        {
            break;
        }

        fields[count++] = character + 1;
    }

    return count;
}

esp_err_t nmea_parse(const char* sentence, gps_location_t* out_location, nmea_sentence_t* out_sentence)
{
    if (sentence == nullptr || out_location == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 1. Validate the sentence and take the part of it that carries the fields.
    char body[MAX_SENTENCE_LEN];
    const esp_err_t err = extract_body(sentence, body, sizeof(body));
    if (err != ESP_OK)
    {
        return err;
    }

    // 2. Split it into the fields the sentence is made of, the first one identifies it.
    char* fields[MAX_FIELDS];
    const size_t count = split_fields(body, fields, MAX_FIELDS);

    const char* identifier = fields[0];
    if (strlen(identifier) <= TALKER_LEN)
    {
        return ESP_ERR_INVALID_RESPONSE;
    }

    // 3. Merge the attributes of the sentences the location data is built from, ignoring the talker
    // that sent them: the same sentence is reported by every constellation the module tracks.
    const char* type = identifier + TALKER_LEN;

    nmea_sentence_t parsed;
    esp_err_t result;

    if (strcmp(type, TYPE_GGA) == 0)
    {
        parsed = NMEA_SENTENCE_GGA;
        result = parse_gga(fields, count, out_location);
    }
    else if (strcmp(type, TYPE_RMC) == 0)
    {
        parsed = NMEA_SENTENCE_RMC;
        result = parse_rmc(fields, count, out_location);
    }
    else if (strcmp(type, TYPE_GSA) == 0)
    {
        parsed = NMEA_SENTENCE_GSA;
        result = parse_gsa(fields, count, out_location);
    }
    else
    {
        return ESP_ERR_NOT_SUPPORTED;
    }

    // 4. Only report which sentence was read once its attributes were merged, a sentence that could
    // not be parsed carries nothing to the location.
    if (result == ESP_OK && out_sentence != nullptr)
    {
        *out_sentence = parsed;
    }

    return result;
}
