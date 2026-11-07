# Publishing

The module publishes exactly what a real device publishes. It must stay consistent with [Reported Data](../device/data/overview.md) and [Location Data](../device/data/location-data.md).

## Topic

The simulated device publishes its location to:

```
/devices/{device_id}/data/location
```

`{device_id}` is the id of the simulated device (`FAKE_DEVICE_ID`). See [Configuration](configuration.md).

## Data format

Reports are serialized in Protocol Buffers, as on a real device. The attributes are the ones in [Location Data](../device/data/location-data.md): `latitude`, `longitude`, `altitude`, `speed` (knots), `heading`, `satellite_count`, `hdop`, `vdop`, `pdop`, `fix_type` and `timestamp`. The values follow the NMEA fields in [NMEA Sentences](../device/nmea/sentences.md).

## Metadata

Each report carries the metadata described in [Reported Data](../device/data/overview.md#metadata):

| Attribute | How the module sets it |
|---|---|
| `timestamp` | Milliseconds since the epoch. It is the simulated clock, so it advances by the reporting interval between two reports. |
| `schema_version` | The version of the data schema the module was built with. |