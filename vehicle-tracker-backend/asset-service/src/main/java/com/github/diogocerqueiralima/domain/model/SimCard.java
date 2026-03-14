package com.github.diogocerqueiralima.domain.model;

import java.util.Objects;

/**
 * Represents a SIM card in the system.
 */
public class SimCard {

    private final String iccid;
    private final String msisdn;
    private final String imsi;

    public SimCard(String iccid, String msisdn, String imsi) {
        this.iccid = Objects.requireNonNull(iccid, "iccid cannot be null");
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
