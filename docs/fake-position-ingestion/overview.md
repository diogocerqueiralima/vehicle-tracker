# Fake Position Ingestion

The fake-position-ingestion module simulates a device reporting GPS location data over MQTT. It lets the backend be developed and tested without real hardware.

> **Warning**: This module is for **development and testing only**. It must **never run in production**. It publishes fake positions on the same topic a real device uses, so running it against a production broker would mix invented data with real vehicle data and could trigger false alerts or corrupt history.

## What it does

The module connects to an MQTT broker and publishes location reports for one simulated device. It supports two modes:

- **Replay**: unrelated locations read from a file that we produce. See [Modes](modes.md).
- **Journey**: a full journey along a route, from the first waypoint to the last. See [Modes](modes.md).

## Where it fits

```
fake-position-ingestion -> MQTT broker -> ingestion-service -> RabbitMQ -> domain services
```

The backend cannot tell the simulated reports from real ones, because they use the same topic and format. See [Publishing](publishing.md).

## Documentation

- [Publishing](publishing.md): topic, data format and the metadata of each report
- [Modes](modes.md): how the simulated positions are produced
- [Configuration](configuration.md): the device, reporting frequency and broker settings
- [Errors](errors.md): how errors are reported

## How to run

The module will be a Gradle module under `vehicle-tracker-backend/`, so it will run with the Gradle wrapper. The module name below is a placeholder until it is created.

1. Start a local MQTT broker, for example Mosquitto. Never point the module at a production broker.
2. Set the configuration through environment variables. See [Configuration](configuration.md).
3. From `vehicle-tracker-backend/`, run:

```
./gradlew :fake-position-ingestion:run
```

The module runs until it is stopped, or until a journey finishes when it is configured to stop at the end.

> **Note**: When the module is implemented, it must be added to `settings.gradle.kts` and to the module matrix in `.github/workflows/build-backend.yml`.
