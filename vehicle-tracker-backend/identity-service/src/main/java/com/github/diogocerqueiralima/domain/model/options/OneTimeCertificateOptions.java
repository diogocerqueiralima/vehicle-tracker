package com.github.diogocerqueiralima.domain.model.options;

public class OneTimeCertificateOptions extends RevokeCertificateOptions {

    private final boolean used;

    public OneTimeCertificateOptions(boolean revoked, boolean used) {
        super(revoked);
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }

    public OneTimeCertificateOptions withUsed(boolean used) {
        return new OneTimeCertificateOptions(this.isRevoked(), used);
    }

}
