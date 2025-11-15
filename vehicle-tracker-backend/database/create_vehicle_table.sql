CREATE SCHEMA IF NOT EXISTS vehicle_tracker_schema;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS "vehicle_tracker_schema".vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vin VARCHAR(17) UNIQUE NOT NULL,
    plate VARCHAR(8) UNIQUE NOT NULL,
    model VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    owner_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC')
);