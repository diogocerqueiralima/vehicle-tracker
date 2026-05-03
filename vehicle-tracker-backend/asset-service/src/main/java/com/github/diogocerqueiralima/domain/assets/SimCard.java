package com.github.diogocerqueiralima.domain.assets;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a SIM card in the system.
 */
public class SimCard extends Asset {

    private final String iccid;
    private final String msisdn;
    private final String imsi;

    public SimCard(UUID id, Instant createdAt, Instant updatedAt, String iccid, String msisdn, String imsi) {
        this(id, null, createdAt, updatedAt, iccid, msisdn, imsi);
    }

    public SimCard(UUID id, UUID ownerId, Instant createdAt, Instant updatedAt, String iccid, String msisdn, String imsi) {
        super(id, ownerId, createdAt, updatedAt);
        this.iccid = Objects.requireNonNull(iccid, "id cannot be null");
        this.msisdn = Objects.requireNonNull(msisdn, "msisdn cannot be null");
        this.imsi = Objects.requireNonNull(imsi, "imsi cannot be null");
    }

    public String getIccid() {
        return iccid;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getImsi() {
        return imsi;
    }

}

