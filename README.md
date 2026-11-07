# VEHICLE TRACKER

Vehicle Tracker is a comprehensive application designed to monitor and manage vehicle locations in real-time.
It provides features such as live tracking, route history, geofencing, alerts for unauthorized movements and more.

## The Main Goal

The primary goal of this project is to practice and demonstrate solid software engineering practices,
including cloud-native development, real-time data processing, automated deployments with GitHub Actions,
and modern system architecture design.

## System Overview

The Vehicle Tracker system consists of a reverse proxy, multiple backend services, Kafka for messaging and a mobile application.

The backend services are responsible for data ingestion, processing, and storage, and expose APIs consumed by the mobile application. More details about each service can be found in `docs/`.

All requests from the mobile application pass through the reverse proxy, which acts as a single entry point and routes traffic to the appropriate backend service.

Vehicle devices installed in vehicles send their data to the reverse proxy. This data is forwarded to Mosquitto. The ingestion service subscribes to it, decodes and validates each message, and publishes it to Kafka as an event. The domain services then process these events asynchronously.

The following diagram illustrates the high-level architecture of the Vehicle Tracker system:

```mermaid
flowchart LR
    subgraph Vehicle Devices
        D1[Device 1]
        D2[Device 2]
        DN[Device N]
    end

    APP[Mobile Client]
    RP[Reverse Proxy]

    subgraph Vehicle Tracker System
        M[Mosquitto]
        K[Kafka]
        IDP[Keycloak]
        DB[(PostgreSQL<br/>TimescaleDB + PostGIS)]

        subgraph Backend Services
            I[Ingestion Service]
            D[Domain Services]
        end
    end

    D1 -->|MQTT over TLS| RP
    D2 -->|MQTT over TLS| RP
    DN -->|MQTT over TLS| RP

    APP -->|HTTP requests| RP
    RP -->|HTTP response| APP

    RP -->|API requests| D
    RP -->|Auth requests| IDP
    RP -->|MQTT| M

    M -->|Protobuf messages| I
    I -->|location events| K
    K -->|events| D

    D --> DB
    D <--> IDP
```

In folder `docs` you can find more detailed documentation about the architecture, deployment, and other aspects of the system.
## Stack

- **Backend**: Java with Spring Boot
- **Mobile**: Kotlin with Jetpack Compose for Android
- **Database**: PostgreSQL with TimescaleDB and PostGIS extensions
- **Messaging**: Kafka, Mosquitto (MQTT broker)
- **Containerization**: Docker (local and cloud deployment)

## Features
- Real-time vehicle tracking
- Route history visualization
- Geofencing setup and notifications
- Alerts for unauthorized movements

## Getting Started
See `docs/deployment.md` for instructions on setting up the system locally or on the cloud.
