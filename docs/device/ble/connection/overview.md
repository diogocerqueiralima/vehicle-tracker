# Connection Service

This document provides the parameters and configuration details for the Connection Service, which is responsible for managing the MQTT connection, such as IP address, port, and other connection-related settings. This service is not responsible for authentication or authorization configuration, which is handled by the Authentication Service.

## Structure

**Service UUID**: `6578878c-85c9-4566-9a51-37c9aaea0413`

| Name | UUID | Type | Actions | Description |
|---|---|---|---|---|
| broker_url | 002af440-7157-45f5-a96f-0c87fa0e5e75 | string | read, write | The URL of the MQTT broker to which the service will connect. |
| keep_alive | 193bdbf3-3f36-4bd1-a06f-e8bc31f1978f | integer | read, write | The keep-alive interval in seconds for the MQTT connection. |
| qos | 497378ba-0252-4d84-a07f-05a886d01186 | integer | read, write | The Quality of Service level for MQTT messages (0, 1, or 2). |
| recon_interval | 1472f295-a20b-4c4b-b191-7f67b0ecaa7c | integer | read, write | The interval in seconds for attempting to reconnect to the MQTT broker if the connection is lost. |
