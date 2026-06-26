# Vehicle Tracker — Embedded Firmware

Firmware for the vehicle tracker hardware device, built with **ESP-IDF** and targeting the **ESP32** microcontroller. The device advertises itself over BLE and exposes a GATT configuration service that allows a mobile companion app to provision MQTT and GPS settings, as well as install device certificates.

## Requirements

| Tool    | Version              |
|---------|----------------------|
| ESP-IDF | >= 4.1.0             |
| CMake   | >= 3.16              |
| NimBLE  | bundled with ESP-IDF |

## Project Structure

```
vehicle-tracker-embedded/
├── CMakeLists.txt
└── main/
    ├── main.c                          Entry point
    ├── ble/
    │   ├── ble_manager.h / .c          NimBLE initialisation & advertising
    │   └── services/
    │       └── configuration_service.h / .c   GATT configuration service
    └── storage/
        └── storage.h / .c             NVS read/write abstraction
```

## Building and Flashing

```bash
# Set up the ESP-IDF environment (once per terminal session)
. $IDF_PATH/export.sh

# Build
idf.py build

# Flash and monitor
idf.py -p /dev/ttyUSB0 flash monitor
```