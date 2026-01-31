package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;

public class Certificate {

    private final CertificateInfo info;
    private final boolean revoked;

    public Certificate(CertificateInfo info, boolean revoked) {
        this.info = info;
        this.revoked = revoked;
    }

    public Certificate(CertificateInfo info) {
        this(info, false);
    }

    public Certificate revoke() {

        if (this.revoked) {
            throw new CertificateRevokedException(this.info);
        }

        return new Certificate(this.info, true);
    }

    public CertificateInfo getInfo() {
        return info;
    }

    public boolean isRevoked() {
        return revoked;
    }

}
