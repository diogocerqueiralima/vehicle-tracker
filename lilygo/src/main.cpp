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

#define MAX_PAYLOAD_SIZE 256
uint8_t payload_buffer[MAX_PAYLOAD_SIZE];
size_t payload_index = 0;

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
 * Callback function que escreve os dados codificados no buffer
 *
 */
bool bufferStreamCallback(pb_ostream_t *stream, const uint8_t *buf, size_t count) {

    if (payload_index + count > MAX_PAYLOAD_SIZE) {
        Serial.println("Buffer overflow!");
        return false;
    }
    
    memcpy(&payload_buffer[payload_index], buf, count);
    payload_index += count;
    return true;
}

/**
 *
 * Creates a new pb_ostream_t for writing to buffer.
 *
 */
pb_ostream_t makeBufferOStream() {
    payload_index = 0; // Reset buffer index
    pb_ostream_t stream;
    stream.callback = bufferStreamCallback;
    stream.state = NULL;
    stream.max_size = MAX_PAYLOAD_SIZE;
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
#define SIM7600_TX 17  // pino TX do ESP32 para RX do SIM7600
#define SIM7600_RX 16  // pino RX do ESP32 para TX do SIM7600
#define SIM7600_BAUD 115200
HardwareSerial simSerial(1); // Serial1

void setup() {
    Serial.begin(115200);
    delay(1000);
    Serial.println("Console AT inicializada");

    // Inicializa Serial1 para o módulo SIM7600
    simSerial.begin(SIM7600_BAUD, SERIAL_8N1, SIM7600_RX, SIM7600_TX);

    // Seu setup existente
    if (!client.init()) {
        Serial.println("Failed to initialize MicroControllerClient");
    } else {
        Serial.println("MicroControllerClient initialized successfully");
    }

    if (!client.gps().enable(GPS_STANDALONE_MODE)) {
        Serial.println("Failed to enable GPS");
    } else {
        Serial.println("GPS enabled successfully");
    }

    //initWifi();
    //initMqtt();
}

void loop() {
    // Parte do envio de localização
    /*GPSInfo info = client.gps().getInfo();
    if (info.valid) {
        size_t encoded_size;
        if (encode_location(info, makeBufferOStream(), &encoded_size)) {
            if (mqttClient.publish(MQTT_TOPIC, payload_buffer, encoded_size)) {
                Serial.print("Location sent, encoded size: ");
                Serial.println(encoded_size);
            } else {
                Serial.println("Failed to publish to MQTT");
            }
        } else {
            Serial.println("Failed to encode location");
        }
    } else {
        Serial.println("No valid GPS data available");
    }*/

    // Parte da console AT
    // 1️⃣ Lê do monitor serial do computador e envia para SIM7600
    if (Serial.available()) {
        String cmd = Serial.readStringUntil('\n');
        cmd += "\r\n"; // comandos AT geralmente precisam de CRLF
        simSerial.print(cmd);
        Serial.print("Enviado para SIM7600: ");
        Serial.println(cmd);
    }

    // 2️⃣ Lê respostas do SIM7600 e imprime no monitor serial
    while (simSerial.available()) {
        char c = simSerial.read();
        Serial.write(c);
    }

    delay(100); // evita travar o loop
}