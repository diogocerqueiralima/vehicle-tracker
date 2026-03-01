package com.github.diogocerqueiralima.domain.model.options;

public class RevokeCertificateOptions implements CertificateOptions {

    private final boolean revoked;

    public RevokeCertificateOptions(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public RevokeCertificateOptions withRevoked(boolean revoked) {
        return new RevokeCertificateOptions(revoked);
    }

}
