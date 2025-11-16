CREATE SCHEMA IF NOT EXISTS vehicle_tracker_schema;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

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

CREATE TABLE IF NOT EXISTS "vehicle_tracker_schema".locations_snapshots (
    id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    point GEOMETRY(POINTZ, 4326) NOT NULL,
    speed DOUBLE PRECISION NOT NULL,
    course DOUBLE PRECISION NOT NULL
)
WITH (
    tsdb.hypertable,
    tsdb.partition_column = 'id',
    tsdb.segmentBy = 'vehicle_id',
    tsdb.chunk_interval = '7 days'
);