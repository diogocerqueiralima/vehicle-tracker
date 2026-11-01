# Location Data

The device reports its location data, which includes information about its geographical position. This data is essential for tracking the device movements.

## Reported Location Data

The following attributes are included in the reported location data:

- `latitude`: The latitude of the device's location in decimal degrees.
- `longitude`: The longitude of the device's location in decimal degrees.
- `altitude`: The altitude of the device's location in meters above sea level.
- `speed`: The speed of the device in knots
- `heading`: The heading or direction over the ground in degrees, where 0° is North, 90° is East, 180° is South, and 270° is West.
- `satellite_count`: The number of satellites used to determine the device's location.
- `hdop`: The horizontal dilution of precision (HDOP) value, which indicates the accuracy of the reported location data. A lower HDOP value indicates better accuracy.
- `vdop`: The vertical dilution of precision (VDOP) value, which indicates the accuracy of the reported altitude data. A lower VDOP value indicates better accuracy.
- `pdop`: The position dilution of precision (PDOP) value, which indicates the accuracy of the reported 3D position (latitude, longitude and altitude combined). A lower PDOP value indicates better accuracy.
- `fix_type`: The type of fix obtained from the satellite system, as an enum: `NO_FIX`, `FIX_2D` or `FIX_3D`.
- `timestamp`: The timestamp of the reported location data from the satellite system, in milliseconds since the epoch (January 1, 1970).

## NMEA Sentences Used

The location data is built from the NMEA sentences output by the GNSS module. The device needs only the following combination, so the other sentences can be disabled on the module:

| Attribute | Sentence | Field |
|---|---|---|
| `latitude`, `longitude` | GGA | 2 to 5 |
| `altitude` | GGA | 9 |
| `satellite_count` | GGA | 7 |
| `hdop` | GSA | 16 |
| `vdop` | GSA | 17 |
| `pdop` | GSA | 15 |
| `fix_type` | GSA | 2 (GSA `1`, `2`, `3` maps to `NO_FIX`, `FIX_2D`, `FIX_3D`) |
| `speed` | RMC | 7 |
| `heading` | RMC | 8 |
| `timestamp` | RMC | 1 and 9 (time and date) |

See [NMEA Sentences](../nmea/sentences.md) for the full field layout of each sentence.

## Location Data Reporting Frequency

The device will report its location data in the following scenarios:

- **Periodic Reporting (moving)**: When the device is moving, it will report its location data at a configurable interval. The default reporting interval is every 30 seconds. This interval can be adjusted based on the device's configuration settings.
- **Periodic Reporting (stationary)**: When the device is stationary, it will report its location data at a configurable interval. The default reporting interval is every 1 hour. This interval can be adjusted based on the device's configuration settings.
- **Wakeup Reporting**: When the device wakes up from a low-power state, it will report its location data immediately. This ensures that the device's location is updated as soon as it becomes active.
- **Event-Triggered Reporting**: The device may also report its location data in response to specific events, such as entering or exiting a predefined geofence area. This allows for real-time tracking of the device's movements in relation to specific locations. 