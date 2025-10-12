#pragma once

#include <Arduino.h>
#include "GPS.h"

#define CONSOLE_BAUD_RATE       115200
#define MODEM_BAUD_RATE         115200

#define MODEM_RX_PIN            26
#define MODEM_TX_PIN            27
#define BOARD_PWRKEY_PIN        4

#define MODEM_POWERON_PULSE_MS  1000
#define MODEM_START_WAIT_MS     1000

#define COMMAND_TIMEOUT_MS      2000

class MicroControllerClient {
public:
    MicroControllerClient();
    bool init();
    GPS& gps() { return _gps; }
    String sendATCommand(const String& cmd);

private:
    GPS _gps;
};