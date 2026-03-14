package com.github.diogocerqueiralima.domain.model.assets;

/**
 * Represents a SIM card asset in the system.
 */
public class SimCard {

    private final String iccid;
    private final String msisdn;
    private final String imsi;

    public SimCard(String iccid, String msisdn, String imsi) {
        this.iccid = iccid;
        this.msisdn = msisdn;
        this.imsi = imsi;
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
