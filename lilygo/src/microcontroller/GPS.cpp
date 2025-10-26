#include "GPS.h"

bool GPS::isEnabled() {

    String cmd = "AT+CGPS?";
    String response = _send(cmd);

    return response.indexOf("+CGPS: 1") >= 0;
}

bool GPS::enable(GPS_MODE mode) {

    if (isEnabled()) {
        return true;
    }

    String cmd = "AT+CGPS=" + String(GPS_ENABLED) + "," + String(mode);
    String response = _send(cmd);
    
    return response.equalsIgnoreCase("OK");
}

bool GPS::disable() {

    if (isDisabled()) {
        return true;
    }

    String cmd = "AT+CGPS=" + String(GPS_DISABLED);
    String response = _send(cmd);

    return response.equalsIgnoreCase("OK");
}

GPSInfo GPS::getInfo() {

    GPSInfo info;
    String resp = _send("AT+CGPSINFO");

    int idx = resp.indexOf("+CGPSINFO:");
    if (idx == -1) return info;

    String data = resp.substring(idx + 10);
    data.trim();

    if (data == "" || data.startsWith("0.0")) return info;

    int start = 0;
    int end = data.indexOf(',');

    auto nextField = [&]() -> String {
        String field = data.substring(start, end);
        start = end + 1;
        end = data.indexOf(',', start);
        return field;
    };

    info.lat = nextField();
    info.NS = nextField()[0];
    info.lon = nextField();
    info.EW = nextField()[0];

    if (info.lat.length() == 0 || info.lon.length() == 0 || info.lat == "0.0" || info.lon == "0.0") {
        info.valid = false;
        return info;
    }

    info.date = nextField();
    info.utcTime = nextField();
    info.altitude = nextField().toFloat();
    info.speed = nextField().toFloat();
    info.course = nextField().toFloat();

    info.valid = true;
    return info;
}
