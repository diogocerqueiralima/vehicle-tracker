# Configuration

The module is configured through environment variables, like the ingestion service. The names below for the broker settings are the same ones the ingestion service uses.

## Broker

| Variable | Description |
|---|---|
| `MQTT_URL` | URL of the MQTT broker. |
| `MQTT_CERTIFICATE_PATH` | Path to the client certificate (PKCS12). |
| `MQTT_CERTIFICATE_PASSWORD` | Password of the client certificate. |

## Simulated device

The module simulates a single device.

| Variable | Description |
|---|---|
| `FAKE_DEVICE_ID` | Id of the simulated device. It is used in the topic the module publishes to. See [Publishing](publishing.md). |

## Mode and frequency

| Variable | Description | Default |
|---|---|---|
| `FAKE_MODE` | `replay` or `journey`. See [Modes](modes.md). | `replay` |
| `FAKE_REPORT_INTERVAL_SECONDS` | Interval between two reports. | `30` |
| `FAKE_MAX_REPORTS` | Number of reports before the module stops. Empty means no limit. | empty |

The default interval matches the default reporting interval of a moving device. See [Location Data](../device/data/location-data.md#location-data-reporting-frequency).

## Replay mode

| Variable | Description | Default |
|---|---|---|
| `FAKE_LOCATIONS_FILE` | Path to the file with the locations to publish. See [Modes](modes.md). | none |
| `FAKE_LOOP` | `true` to start again after the last location, `false` to stop. | `false` |

## Journey mode

| Variable | Description | Default |
|---|---|---|
| `FAKE_ROUTE_FILE` | Path to the file with the ordered waypoints. | none |
| `FAKE_LOOP` | `true` to start again after the last waypoint, `false` to stop. Same variable as in replay mode. | `false` |

The speed and the events are not environment variables. They are defined in the route file, so each part of the journey can be different:

- **Speed**: every waypoint except the first has a `speed_knots`, which is the speed on the segment from the previous waypoint to that one. It is required.
- **Events**: the route file can contain the events `stop`, `no_fix` and `silence`. The fields of an event are listed in [Event fields](modes.md#event-fields).

See [Modes](modes.md#journey-events).
