# Project Overview

Repo hold full codebase for **Vehicle Tracker system**.

Components:

- Backend services (Asset Service, Identity Service, etc.)
- Web frontend
- Mobile application
- Embedded software for devices
- Database schemas

Purpose: collect vehicle telemetry from hardware devices, expose to users via web + mobile. Users track vehicles real-time, view history, manage assets.

# Repository Structure

Independent directories:

- '/vehicle-tracker-backend': all backend services (Asset Service, Identity Service, others). Java + Spring Boot.
- '/vehicle-tracker-frontend': web frontend. Typescript + React.
- '/vehicle-tracker-mobile': mobile app. Kotlin + Jetpack Compose.
- '/vehicle-tracker-embedded': embedded software for hardware devices. C + esp-idf.

Agents must **identify the correct module before making changes**.

# Backend Architecture

Backend follow **Hexagonal Architecture** (Ports and Adapters). Each service independent + modular for easy maintenance and scaling. Services talk via gRPC or event-driven Kafka, depend on use case.

Backend services own business logic, data persistence, external system communication.

Layers per service:

### Presentation Layer

Handle incoming requests, send responses.
Build Command objects from client DTOs, pass to Application Layer, get Result object back, convert to DTO, send to client.
**No business logic here.**

Examples:
- REST controllers
- gRPC service implementations
- WebSocket handlers
- Kafka consumers

### Application Layer

Implement inbound ports defined in Domain Layer, orchestrate use cases. Convert Command objects from Presentation Layer to Domain objects, call Domain Layer with them (domain service method or aggregate root).

Invoke outbound ports to reach external services or repositories. No logic about *how* to call them (no HTTP requests, gRPC calls, database queries here).

### Domain Layer

Hold business logic: domain entities, value objects, domain services, other business classes. **No persistence code, no external service calls.**

### Infrastructure Layer

Implement outbound ports defined in Domain Layer. Hold persistence code (database repositories) and external service calls (HTTP clients, gRPC clients, Kafka producers). **No business logic.**

# Frontend Architecture

Web frontend use component-based architecture with React. Layers:

### UI Layer

Render user interface, handle user interaction. Hold React components, styles, presentation code. **No business logic.**

### Service Layer

Talk to backend services. Hold HTTP request code, response handling, other backend communication. **No business logic.**

### Domain Layer

Hold business logic: business rules and logic code. **No backend communication code, no UI rendering.**

# Mobile Architecture

Mobile app follow architecture like web frontend, Jetpack Compose for UI. Layers:

### Presentation Layer

Render user interface, handle user interaction. Hold Jetpack Compose components, styles, presentation code. **No business logic.**

### Domain Layer

Hold business logic: business rules and logic code. **No backend communication code, no UI rendering.**

### Infrastructure Layer

Talk to backend services. Hold HTTP request code, response handling, DataStore persistence, other backend communication. **No business logic.**

# Embedded Architecture

Embedded software follow no specific architecture pattern for now, but organize for easy maintenance and scaling. Code modular, well-structured, clear separation of concerns. Embedded software collect telemetry from hardware devices, send to backend services. Also handle hardware component communication, ensure reliable transmission to backend.

`vehicle-tracker-embedded` module compile with `-std=gnu23` (GNU dialect of C23). C23 features like `nullptr` available and already used in codebase.

# Database Schema Changes

Agents MUST follow these rules when modifying the database schema:

1. Any schema change MUST include a migration script.
2. Never apply schema changes manually.
3. Migration must be idempotent and safe to run in production.
4. Migration script must ship in same change set as code that needs it.
5. Agents MUST update domain models and persistence mappings on schema change.

# Testing

New features or bug fixes need tests. Cover unit and integration scenarios, depend on change nature. Organize tests so what is tested and why stay obvious.

# General Rules for Agents

Agents on this repo must:

1. Respect architecture of each module.
2. Avoid cross-module dependencies.
3. Do not introduce new frameworks without justification.
4. Prefer small, incremental changes.
5. Maintain readability and consistency.

# Code Modification Guidelines

When modifying code, agents should:

1. Follow existing coding conventions and styles.
2. Do not break existing functionality.
3. Add comments and docs where needed to explain change purpose. Javadoc for public methods and classes, inline comments for complex logic inside methods.
4. Inline comments use numbered enumerations — "1.", "1.1", "2.", etc. — to mark steps or logic explained. Helps readability, especially complex sections.

# Goal for AI Agents

Agents should assist with:

- implementing new features
- fixing bugs
- improving code quality
- writing tests
- maintaining architectural boundaries