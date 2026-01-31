package com.github.diogocerqueiralima.infrastructure.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "bootstrap_certificates", schema = "vehicle_tracker_schema")
@PrimaryKeyJoinColumn(name = "serial_number")
public class BootstrapCertificateEntity extends CertificateEntity {

    @Column(nullable = false)
    private boolean used;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

}
