CREATE SCHEMA IF NOT EXISTS vehicle_tracker_schema;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

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