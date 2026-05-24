# Device Configuration Overview

Devices in the vehicle-tracker system are configured using a set of parameters that define their behavior and how they interact with the system. This overview provides a high-level understanding of the key configuration parameters for devices.

> **Note**: This system is still under development. More parameters may be added in the future.

## Key Configuration Parameters

The configuration parameters are categorized into several groups based on their functionality:

1. **Connection Parameters**
2. **GPS Parameters**
3. **Authentication Parameters**

### Connection Parameters

- **MQTT Broker URL**: The URL of the MQTT broker that the device will connect to for sending and receiving messages.
- **MQTT Keep Alive Interval**: The interval at which the device will send a keep-alive message to the MQTT broker to maintain the connection. This is typically measured in seconds.
- **MQTT Quality of Service (QoS)**: The level of service for message delivery. Common QoS levels include 0 (at most once), 1 (at least once), and 2 (exactly once). The choice of QoS can affect the reliability and performance of message delivery.
- **MQTT Reconnection Interval**: The interval at which the device will attempt to reconnect to the MQTT broker if the connection is lost. This is typically measured in seconds.
- **MQTT Topic**: The topic to which the device will publish its data.

### GPS Parameters

- **GPS Update Interval**: The frequency at which the device will update its GPS location. This is typically measured in seconds. A shorter interval means more frequent updates, which can provide more accurate tracking but may consume more battery power. The device will only update its location if it is moving, which helps to conserve battery life when the vehicle is stationary.
- **GPS Timeout**: The maximum amount of time the device will wait for a GPS fix before giving up and sending an error message. This is typically measured in seconds.

### Authentication Parameters

- **Certificate Signing Request (CSR)**: The device can generate a CSR that can be submitted to the Identity Service to obtain a certificate for authenticating with the MQTT broker. The device exposes a CSR generation operation via BLE, allowing users to easily obtain the CSR and install the resulting certificate back on the device.
- **Certificate**: The certificate issued by the Identity Service and installed on the device to authenticate with the MQTT broker. The device exposes a CSR generation operation via BLE so the user can obtain the CSR, submit it to the Identity Service, and install the resulting certificate back on the device.

## BLE Configuration

Device parameters can be configured via BLE. For more information on the BLE protocol and the Configuration Service, refer to the [BLE Overview](../overview.md) and the [BLE Configuration Service](protocol-specification.md).