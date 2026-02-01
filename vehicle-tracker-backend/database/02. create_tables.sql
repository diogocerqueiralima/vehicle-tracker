CREATE TABLE IF NOT EXISTS vehicles (
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

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    imei VARCHAR(15) UNIQUE NOT NULL,
    serial_number VARCHAR(255) UNIQUE NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    vehicle_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS certificates (
    serial_number VARCHAR(255) PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS bootstrap_certificates (
    serial_number VARCHAR(255) PRIMARY KEY,
    used BOOLEAN NOT NULL,
    CONSTRAINT FK_serial_number FOREIGN KEY (serial_number)
        REFERENCES certificates(serial_number) ON DELETE CASCADE
);