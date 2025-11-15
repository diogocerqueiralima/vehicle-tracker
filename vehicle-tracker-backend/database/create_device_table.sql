CREATE SCHEMA IF NOT EXISTS vehicle_tracker_schema;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS "vehicle_tracker_schema".devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    imei VARCHAR(15) UNIQUE NOT NULL,
    serial_number VARCHAR(255) UNIQUE NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    vehicle_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    FOREIGN KEY (vehicle_id) REFERENCES "vehicle_tracker_schema".vehicles(id) ON DELETE CASCADE
);