package com.github.diogocerqueiralima.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

@Configuration
public class KeyStoreConfig {

    @Value("${keystore.pkcs11.config}")
    private String pkcs11Config;

    @Value("${keystore.pkcs11.pin}")
    private String pkcs11Pin;

    @Bean
    public Provider provider() {
        return Security.getProvider("SunPKCS11")
            .configure(pkcs11Config);
    }

    @Bean
    public KeyStore keyStore(Provider provider)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        KeyStore keyStore = KeyStore.getInstance("PKCS11", provider);
        keyStore.load(null, pkcs11Pin.toCharArray());

        return keyStore;
    }

}
