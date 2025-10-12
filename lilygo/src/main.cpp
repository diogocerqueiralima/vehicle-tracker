#include "microcontroller/MicroControllerClient.h"
#include <WiFi.h>
#include <PubSubClient.h>
#include "vehicle-tracker-schemas/Location.pb.h"
#include "encoders/LocationEncoder.h"
#include <pb_encode.h>
#include "secrets.h"

MicroControllerClient client;
WiFiClient espClient;
PubSubClient mqttClient(espClient);

/**
 * WiFi initialization, this function connects to the WiFi network using the credentials defined in secrets.h
 * It blocks until the connection is established.
 * 
 * This function is only for demonstration purposes. In a production application, it will be necessary to implement
 * with mobile network.
 * 
 */
void initWifi() {

    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to WiFi");

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }

    Serial.println("Connected to WiFi");
}

/**
 * 
 * MQTT initialization, this function connects to the MQTT broker using the credentials defined in secrets.h
 * It blocks until the connection is established.
 * 
 */
void initMqtt() {

    while (!mqttClient.connected()) {

        Serial.print("Connecting to MQTT...");

        mqttClient.setServer(MQTT_IP, MQTT_PORT);
        if (mqttClient.connect("ESP32Client", MQTT_USERNAME, MQTT_PASSWORD)) {
            Serial.println("MQTT connection successful");
        } else {
            Serial.print("MQTT connection failed, rc=");
            Serial.print(mqttClient.state());
            Serial.println(" try again in 5 seconds");
            delay(5000);
        }

    }

}

/**
 * 
 * Creates a callback function for the pb_ostream_t that publishes the encoded data to the MQTT broker.
 * 
 */
bool mqttStreamCallback(pb_ostream_t *stream, const uint8_t *buf, size_t count) {
    
    if (mqttClient.connected()) {
        return mqttClient.publish(MQTT_TOPIC, buf, count);
    } else {
        Serial.println("MQTT not connected");
        return false;
    }

}

/**
 * 
 * Creates a new pb_ostream_t for publishing messages to MQTT.
 * 
 */
pb_ostream_t makeMqttOStream() {
    pb_ostream_t stream;
    stream.callback = mqttStreamCallback;
    stream.state = NULL;
    stream.max_size = SIZE_MAX;
    stream.bytes_written = 0;
    return stream;
}

/**
 * 
 * Deep Sleep does not mantains the cpu and ram powered, so the state is lost.
 * Light sleep mantains the ram and cpu powered, so the state is mantained.
 * 
 * So, to send the location every 15 seconds, we can use light sleep,
 * but when the device is completely immobilized (vehicle is in the same place for a long time),
 * we can use deep sleep to save power, and when the accelerometer detects movement, we can wake up the device.
 * 
 */

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

    initWifi();
    initMqtt();
}

void loop() {

    GPSInfo info = client.gps().getInfo();
    schemas_v1_location_Location location = schemas_v1_location_Location_init_default;

    if (info.valid) {

        size_t encoded_size;
        encode_location(info, makeMqttOStream(), &encoded_size);

        Serial.print("Location sent, encoded size: "); Serial.println(encoded_size);
    } else {
        Serial.println("No valid GPS data available");
    }

    delay(5000);
}

