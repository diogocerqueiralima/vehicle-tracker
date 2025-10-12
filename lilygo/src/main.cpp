#include "MicroControllerClient.h"

MicroControllerClient client;

void setup() {

    if (!client.init()) {
        Serial.println("Failed to initialize MicroControllerClient");
        return;
    }
    
    Serial.println("MicroControllerClient initialized successfully");

    if (!client.gps().enable(GPS_STANDALONE_MODE)) {
        Serial.println("Failed to enable GPS");
        return;
    }

    Serial.println("GPS enabled successfully");
}

void loop() {

    GPSInfo info = client.gps().getInfo();
    if (info.valid) {
        Serial.print("Latitude: "); Serial.print(info.lat); Serial.print(" "); Serial.println(info.NS);
        Serial.print("Longitude: "); Serial.print(info.lon); Serial.print(" "); Serial.println(info.EW);
        Serial.print("Date: "); Serial.println(info.date);
        Serial.print("UTC Time: "); Serial.println(info.utcTime);
        Serial.print("Altitude: "); Serial.println(info.altitude);
        Serial.print("Speed: "); Serial.println(info.speed);
        Serial.print("Course: "); Serial.println(info.course);
    } else {
        Serial.println("No valid GPS data available");
    }

    delay(5000);

}

