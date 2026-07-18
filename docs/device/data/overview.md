# Reported Data

The data reported by the device is organized into a hierarchical structure of objects, each containing specific attributes. This structure allows for efficient data management and retrieval.

You can see the different reported data in the following sections:

- [Location Data](location-data.md): Information about the device's location
- [Network Data](network-data.md): Details about the device's network connectivity
- [Status Data](status-data.md): Information about the device's performance and operational status

Each section provides a comprehensive overview of the reported data, including the attributes and their respective values. This organization helps users understand the data being collected and how it can be utilized for various applications.

> **Note**: This is still a work in progress, and more data types may be added in the future as the device evolves.

## Metadata

Each reported data object includes metadata that provides additional context about the data being reported. The metadata includes the following attributes:

- `timestamp`: The time at which the data was reported in milliseconds since the epoch (January 1, 1970).
- `sequence_number`: A number that increments with each reported data object, allowing for tracking the order of data reporting.
- `schema_version`: The version of the data schema being used, which helps ensure compatibility between the device and the data processing system.

## How the device reports data

Each device has a MQTT Topic that it uses to report data. The topic is structured as follows:

```
/devices/{device_id}/data/{data_type}
```

Only the device with the specified `{device_id}` can publish data to this topic. The `{data_type}` specifies the type of data being reported, such as location, network, or status.

The QoS (Quality of Service) level for the MQTT messages is a configurable connection parameter (see the `qos` parameter in the [Device Configuration Overview](../ble/configuration/overview.md)) and defaults to 0, meaning messages are delivered at most once with no acknowledgment required. QoS 0 is an appropriate default because the device reports data at a high frequency, so occasional message loss is acceptable and can be compensated for by subsequent reports. The `sequence_number` metadata attribute can be used to detect any missing data reports and send an alert if necessary.

## Data Format

The device publishes data to the MQTT topic using Protocol Buffers (protobuf) format, which is a language-neutral, platform-neutral, extensible mechanism for serializing structured data. This allows for efficient data transmission and ensures compatibility across different platforms and programming languages.

In this project, we use the [NanoPB](https://github.com/nanopb/nanopb) library to serialize and deserialize the data in protobuf format. The library provides a lightweight implementation of Protocol Buffers, making it suitable for resource-constrained devices.

## Data Reporting Frequency

The frequency at which the device reports data can vary based on the configuration and the type of data being reported. Some data types may be reported in real-time, while others may be sent at regular intervals or upon specific events that will be explained in the respective sections.
