# Device Tests

This document explains how to write and run tests for the device firmware.

> **Note**: This system is still under development. More information may be added in the future.

## Test levels

- **Unit tests**: verify a single piece of code in isolation. Anything it depends on is replaced by a test double, so the test only fails when the code under test is wrong.
- **Integration tests**: verify that several pieces work together. They run on a real device, because they depend on the ESP32 peripherals and on ESP-IDF components.

## Where tests run

| Environment        | Used for                                        |
|--------------------|-------------------------------------------------|
| Host (Linux)       | Unit tests of code that does not touch hardware |
| ESP32 (real board) | Integration tests and hardware-dependent code   |

Prefer the host, since it is faster and does not need a board. Use the board only when the test cannot run on the host.

## Writing a test

Tests are written in C with the Unity framework, which is bundled with ESP-IDF.

1. Create the test file in `main_test/main`, inside a folder named after the component it verifies, as `<component>/<component>_test.c`. Files ending in `_test.c` are picked up automatically.
2. Group related tests in the same file, named after what they exercise.
3. Name each test after the behavior and the condition being checked, not after the function being called.
4. Keep each test focused on one behavior, so a failure points directly to what broke.
5. Replace external dependencies with test doubles in unit tests.

Tests live in the `main_test` app, not inside the components, so the firmware build never includes the test framework. See the [Project structure](../overview.md#project-structure) for the full layout.

## Running tests

Tests are not part of the firmware. They are built as their own app, `main_test`, which holds the tests and includes the components. Run all the commands below from the `main_test` folder:

```bash
cd vehicle-tracker-embedded/main_test
```

### On the host

Build and run the test application on the Linux target:

```bash
idf.py --preview set-target linux
idf.py build
idf.py monitor
```

### On the board

Connect the board, then build, flash and open the monitor:

```bash
idf.py set-target esp32
idf.py -p <PORT> flash monitor
```

The results are printed on the serial monitor. Exit it with `Ctrl+]`.

## References

- [Unit Testing in ESP32](https://docs.espressif.com/projects/esp-idf/en/stable/esp32/api-guides/unit-tests.html) - Official documentation for testing ESP-IDF projects.
- [Linux Host Testing](https://docs.espressif.com/projects/esp-idf/en/stable/esp32/api-guides/host-apps.html) - Running ESP-IDF code on the host machine.
