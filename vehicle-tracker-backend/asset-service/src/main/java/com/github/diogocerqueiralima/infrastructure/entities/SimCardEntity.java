package com.github.diogocerqueiralima.infrastructure.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sim_cards")
public class SimCardEntity {

    @Id
    @Column(name = "iccid", nullable = false, updatable = false, length = 32)
    private String iccid;

    @Column(name = "msisdn", nullable = false, unique = true, length = 32)
    private String msisdn;

    @Column(name = "imsi", nullable = false, unique = true, length = 32)
    private String imsi;

    public SimCardEntity() {}

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

}

