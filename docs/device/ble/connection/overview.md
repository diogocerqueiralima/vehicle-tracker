# Connection Service

This document provides the parameters and configuration details for the Connection Service, which is responsible for managing the MQTT connection, such as IP address, port, and other connection-related settings. This service is not responsible for authentication or authorization configuration, which is handled by the Authentication Service.

## Structure

**Service UUID**: `1304eaaa-c937-511a-6605-c9858c877865`

| Name | UUID | Type | Actions | Description | Default Value |
|---|---|---|---|---|---|
| broker_url | 755e0efa-870c-6f29-f505-577140f42a00 | string | read, write | The URL of the MQTT broker to which the service will connect. | - |
| keep_alive | 8f97f131-bce8-6f20-d10b-363ff3db3b19 | integer | read, write | The keep-alive interval in seconds for the MQTT connection. | 60 |
| qos | 8611d086-a805-7f20-840d-5202ba787349 | integer | read, write | The Quality of Service level for MQTT messages (0, 1, or 2). | 0 |
| recon_interval | 7caaecb0-677f-9131-4b0c-0ba295f27214 | integer | read, write | The interval in seconds for attempting to reconnect to the MQTT broker if the connection is lost. | 30 |
