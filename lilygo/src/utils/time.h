#pragma once

#include <time.h>

const long UTC_OFFSET_SECONDS = 0;

int64_t getDateTimeToUnix(const String &date, const String &utcTime) {
    struct tm t = {0};

    t.tm_mday = date.substring(0,2).toInt();
    t.tm_mon  = date.substring(2,4).toInt() - 1;
    t.tm_year = date.substring(4,6).toInt() + 100;

    String timeStr = utcTime;
    int dotIndex = timeStr.indexOf('.');
    if(dotIndex > 0) timeStr = timeStr.substring(0, dotIndex);
    while(timeStr.length() < 6) timeStr += '0';
    t.tm_hour = timeStr.substring(0,2).toInt();
    t.tm_min  = timeStr.substring(2,4).toInt();
    t.tm_sec  = timeStr.substring(4,6).toInt();

    time_t localTime = mktime(&t);
    return (int64_t)(localTime - UTC_OFFSET_SECONDS);
}

