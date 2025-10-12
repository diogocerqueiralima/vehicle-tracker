#include "MicroControllerClient.h"

HardwareSerial SerialAT(1);

bool checkRespond() {

    for (int i = 0; i < 10; i++) {

        SerialAT.print("AT\r\n");

        String input = SerialAT.readString();
        if (input.indexOf("OK") >= 0) {
            return true;
        }

        delay(200);
    }
    
    return false;
}

uint32_t AutoBaud() {

    uint32_t rates[] = {115200, 9600, 57600, 38400, 19200};

    for (uint8_t i = 0; i < sizeof(rates) / sizeof(rates[0]); i++) {

        Serial.print("Trying baud rate "); Serial.println(rates[i]);

        SerialAT.updateBaudRate(rates[i]);

        delay(50);

        for (int j = 0; j < 5; j++) {

            SerialAT.print("AT\r\n");

            String input = SerialAT.readString();

            if (input.indexOf("OK") >= 0) {
                Serial.print("Modem responded at rate: "); Serial.println(rates[i]);
                return rates[i];
            }

        }

    }
    SerialAT.updateBaudRate(MODEM_BAUD_RATE);
    return 0;
}

MicroControllerClient::MicroControllerClient()
    : _gps([this](const String& cmd){ return sendATCommand(cmd); })
{}

bool MicroControllerClient::init() {
    
    Serial.begin(CONSOLE_BAUD_RATE);
    SerialAT.begin(MODEM_BAUD_RATE, SERIAL_8N1, MODEM_RX_PIN, MODEM_TX_PIN);

    pinMode(BOARD_PWRKEY_PIN, OUTPUT);
    digitalWrite(BOARD_PWRKEY_PIN, LOW);
    delay(100);
    digitalWrite(BOARD_PWRKEY_PIN, HIGH);
    delay(MODEM_POWERON_PULSE_MS);
    digitalWrite(BOARD_PWRKEY_PIN, LOW);

    while (!checkRespond()) {
        Serial.println("Awaiting modem startup...");
        delay(MODEM_START_WAIT_MS);
    }

    if (!AutoBaud()) {
        return false;
    }

    return true;
}

String MicroControllerClient::sendATCommand(const String& cmd) {

    SerialAT.println(cmd);

    String resp;
    unsigned long start = millis();
    String line;

    while (millis() - start < COMMAND_TIMEOUT_MS) {
        while (SerialAT.available()) {

            char c = SerialAT.read();
            if (c == '\r') continue;

            if (c == '\n') {
                line.trim();
                if (line.length() > 0 && line != cmd) {
                    if (resp.length() > 0) resp += "\n";
                    resp += line;
                }
                line = "";
            } else {
                line += c;
            }

        }
    }

    return resp;
}