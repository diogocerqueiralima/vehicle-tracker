CREATE TABLE IF NOT EXISTS assets (
    id UUID PRIMARY KEY,
    owner_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC')
);

CREATE TABLE IF NOT EXISTS vehicles (
    id UUID PRIMARY KEY,
    vin VARCHAR(255) UNIQUE NOT NULL,
    plate VARCHAR(255) UNIQUE NOT NULL,
    model VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    manufacturing_date DATE NOT NULL,
    CONSTRAINT fk_vehicles_assets FOREIGN KEY (id) REFERENCES assets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY,
    serial_number VARCHAR(255) UNIQUE NOT NULL,
    model VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    imei VARCHAR(255) UNIQUE NOT NULL,
    CONSTRAINT fk_devices_assets FOREIGN KEY (id) REFERENCES assets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sim_cards (
    iccid VARCHAR(32) PRIMARY KEY,
    msisdn VARCHAR(32) UNIQUE NOT NULL,
    imsi VARCHAR(32) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS assignments (
    id BIGSERIAL PRIMARY KEY,
    assigned_at TIMESTAMPTZ NOT NULL,
    unassigned_at TIMESTAMPTZ,
    assigned_by UUID NOT NULL,
    unassigned_by UUID,
    active BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicle_assignments (
    id BIGSERIAL PRIMARY KEY,
    device_id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    removal_reason VARCHAR(32),
    installed_by UUID,
    notes VARCHAR(1024),
    CONSTRAINT fk_vehicle_assignments_assignments FOREIGN KEY (id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_vehicle_assignments_devices FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_vehicle_assignments_vehicles FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sim_card_assignments (
    id BIGSERIAL PRIMARY KEY,
    device_id UUID NOT NULL,
    sim_card_iccid VARCHAR(32) NOT NULL,
    removal_reason VARCHAR(255),
    CONSTRAINT fk_sim_card_assignments_assignments FOREIGN KEY (id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_sim_card_assignments_devices FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_sim_card_assignments_sim_cards FOREIGN KEY (sim_card_iccid) REFERENCES sim_cards(iccid) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS certificates (
    serial_number numeric(38, 0) PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS bootstrap_certificates (
    serial_number numeric(38, 0) PRIMARY KEY,
    used BOOLEAN NOT NULL,
    CONSTRAINT FK_serial_number FOREIGN KEY (serial_number)
        REFERENCES certificates(serial_number) ON DELETE CASCADE
);