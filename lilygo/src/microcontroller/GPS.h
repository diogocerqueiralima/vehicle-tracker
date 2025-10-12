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

/**
 * 
 * GPS status and mode enums.
 * 
 */
enum GPS_STATUS {
    GPS_DISABLED = 0,
    GPS_ENABLED = 1
};

enum GPS_MODE {
    GPS_STANDALONE_MODE = 1, // Based on satellite data only without network assistance, it is the default mode
    GPS_UE_BASED_MODE = 2, // Based on network assistance
    GPS_UE_ASSISTED_MODE = 3 // Based on network and satellite assistance
};

class GPS {
public:
    using SendFunc = std::function<String(const String&)>;

    /**
     * 
     * Constructor that takes a function to send AT commands to the modem.
     * 
     */
    GPS(SendFunc sendFunc) : _send(sendFunc) {}

    /**
     * 
     * Enables the GPS with the specified mode.
     * If the GPS is already enabled, it does nothing and returns true.
     * 
     */
    bool enable(GPS_MODE mode);

    /**
     * 
     * Disables the GPS.
     * If the GPS is already disabled, it does nothing and returns true.
     * 
     */
    bool disable();

    /**
     * 
     * Checks if the GPS is enabled.
     * 
     */
    bool isEnabled();

    /**
     * 
     * Checks if the GPS is disabled.
     * 
     */
    bool isDisabled() { return !isEnabled(); }

    /**
     * 
     * Gets the current GPS information.
     * If the GPS is not enabled or there is no fix, it returns an empty GPSInfo struct.
     * 
     */
    GPSInfo getInfo();

private:
    SendFunc _send;
};
