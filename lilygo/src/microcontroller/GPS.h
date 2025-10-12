#pragma once

#include <Arduino.h>

struct GPSInfo {

    String lat;
    char NS;
    String lon;
    char EW;
    String date;
    String utcTime;
    float altitude;
    float speed;
    float course;

    bool valid = false;
};

enum GPS_STATUS {
    GPS_DISABLED = 0,
    GPS_ENABLED = 1
};

enum GPS_MODE {
    GPS_STANDALONE_MODE = 1,
    GPS_UE_BASED_MODE = 2,
    GPS_UE_ASSISTED_MODE = 3
};

class GPS {
public:
    using SendFunc = std::function<String(const String&)>;
    GPS(SendFunc sendFunc) : _send(sendFunc) {}
    bool enable(GPS_MODE mode);
    bool disable();
    bool isEnabled();
    bool isDisabled() { return !isEnabled(); }
    GPSInfo getInfo();

private:
    SendFunc _send;
};
