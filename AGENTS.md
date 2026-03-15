# Project Overview

This repository contains the full codebase for a **Vehicle Tracker system**.

The system consists of multiple components, including:

- Backend services (Asset Service, Identity Service, etc.)
- Web frontend
- Mobile application
- Embedded software for devices
- Database schemas

The purpose of this project is to collect vehicle telemetry data from hardware devices and make it accessible to users through a web interface and mobile app. The system allows users to track their vehicles in real-time, view historical data, and manage their assets effectively.

# Repository Structure

The repository is organized into several independent directories:

- '/vehicle-tracker-backend': Contains the source code for all backend services, including the Asset Service, Identity Service, and other related services written in Java with Spring Boot.
- '/vehicle-tracker-frontend': Contains the source code for the web frontend application written in Typescript with React.
- '/vehicle-tracker-mobile': Contains the source code for the mobile application written in Kotlin with Jetpack Compose.
- '/vehicle-tracker-embedded': Contains the source code for the embedded software that runs on the hardware devices, written in C with esp-idf.

Agents must **identify the correct module before making changes**.

# Backend Architecture

The backend follows **Hexagonal Architecture** (also known as Ports and Adapters). Each service is designed to be independent and modular, allowing for easy maintenance and scalability. The services should communicate with each other through gRPC or event-driven architecture with Kafka, depending on the use case. 

The backend services are responsible for handling business logic, data persistence, and communication with external systems.

Each backend service is divided into several layers:

### Presentation Layer

Responsible for handling incoming requests and sending responses.
This layer should create Command objects with the DTOs received from the client and pass them to the Application Layer that will return a Result object that the Presentation Layer will convert to a DTO and send back to the client.
This layer should **not contain any business logic**. 

Examples:
- REST controllers
- gRPC service implementations
- WebSocket handlers
- Kafka consumers

### Application Layer

Responsible for implement inbound ports defined in the Domain Layer and orchestrating the use cases. This layer should convert the Command objects received from the Presentation Layer to Domain objects and use the converted Domain objects to call the Domain Layer (e.g., by calling a method on a domain service or an aggregate root).

This layer should invoke the outbound ports to call external services or repositories, but it should not contain any logic related to how to call those services (e.g., it should not contain any code related to HTTP requests, gRPC calls, database queries, etc.).

### Domain Layer

Responsible for implementing the business logic of the application. This layer should contain the domain entities, value objects, domain services, and any other classes that are related to the business logic of the application. This layer should **not contain any code related to how to persist data or how to call external services**.

### Infrastructure Layer

Responsible for implementing the outbound ports defined in the Domain Layer. This layer should contain the code related to how to persist data (e.g., database repositories) and how to call external services (e.g., HTTP clients, gRPC clients, Kafka producers, etc.). This layer should **not contain any business logic**.

# Frontend Architecture

The frontend application (web) follows a component-based architecture using React. The application is organized into several layers:

### UI Layer

Responsible for rendering the user interface and handling user interactions. This layer should contain React components, styles, and any other code related to the presentation of the application. This layer should **not contain any business logic**.

### Service Layer

Responsible for handling communication with the backend services. This layer should contain code related to making HTTP requests, handling responses, and any other code related to communication with the backend. This layer should **not contain any business logic**.

### Domain Layer

Responsible for implementing the business logic of the application. This layer should contain any code related to the business rules and logic of the application. This layer should **not contain any code related to how to communicate with the backend or how to render the UI**.

# Mobile Architecture

The mobile application follows a similar architecture to the web frontend, using Jetpack Compose for the UI. The layers are:

### Presentation Layer

Responsible for rendering the user interface and handling user interactions. This layer should contain Jetpack Compose components, styles, and any other code related to the presentation of the application. This layer should **not contain any business logic**.

### Domain Layer

Responsible for implementing the business logic of the application. This layer should contain any code related to the business rules and logic of the application. This layer should **not contain any code related to how to communicate with the backend or how to render the UI**.

### Infrastructure Layer

Responsible for handling communication with the backend services. This layer should contain code related to making HTTP requests, handling responses, storing data using DataStore, and any other code related to communication with the backend. This layer should **not contain any business logic**.

# Embedded Architecture

For now, the embedded software does not follow a specific architecture pattern, but it should be organized in a way that allows for easy maintenance and scalability. The code should be modular and well-structured, with clear separation of concerns. The embedded software is responsible for collecting telemetry data from the hardware devices and sending it to the backend services. It should also handle any necessary communication with the hardware components and ensure reliable data transmission to the backend services.

# Database Schema Changes

Agents MUST follow these rules when modifying the database schema:

1. Any schema change MUST include a migration script.
2. Schema changes must never be applied manually.
3. The migration must be idempotent and safe to run in production environments.
4. The migration script must be included in the same change set as the code that requires it.

# Testing

When implementing new features or fixing bugs, agents should also write tests to ensure the correctness of their changes. The tests should cover both unit and integration scenarios, depending on the nature of the change. Tests should be organized in a way that makes it easy to understand what is being tested and why.

# General Rules for Agents

Agents working on this repository must follow these rules:

1. Respect the architecture of each module.
2. Avoid cross-module dependencies.
3. Do not introduce new frameworks without justification.
4. Prefer small, incremental changes.
5. Maintain readability and consistency.

# Code Modification Guidelines

When modifying code, agents should:

1. Follow existing coding conventions and styles.
2. Ensure that their changes do not break existing functionality.
3. Add comments and documentation where necessary to explain the purpose of their changes. Javadoc comments should be used for public methods and classes, while inline comments can be used for complex logic within methods.
4. Inline comments should use numbered enumerations such as "1.", "1.1", "2.", etc., to clearly indicate the steps or logic being explained. This helps in improving readability and understanding of the code, especially for complex sections.