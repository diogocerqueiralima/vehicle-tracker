package com.github.diogocerqueiralima.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

@Configuration
public class KeyStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(KeyStoreConfig.class);
    @Value("${keystore.pkcs11.config}")
    private String pkcs11Config;

    @Value("${keystore.pkcs11.pin}")
    private String pkcs11Pin;

    @Value("${keystore.certificate.alias}")
    private String certificateAlias;

    @Value("${keystore.key.alias}")
    private String keyAlias;

    @Value("${keystore.key.password}")
    private String keyPassword;

    @PostConstruct
    public void init() {

        Security.addProvider(
                Security.getProvider("SunPKCS11")
                        .configure(pkcs11Config)
        );

        Security.addProvider(new BouncyCastleProvider());

    }

    @Bean
    public KeyStore keyStore() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        KeyStore keyStore = KeyStore.getInstance("PKCS11");
        keyStore.load(null, pkcs11Pin.toCharArray());

        return keyStore;
    }

    @Bean
    public KeyPair keyPair(KeyStore keyStore)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {

        Key key = keyStore.getKey(keyAlias, keyPassword.toCharArray());

        if (!(key instanceof PrivateKey privateKey)) {
            throw new KeyStoreException("The key retrieved is not a private key");
        }

        log.info("Private key algorithm: {}", privateKey.getAlgorithm());

        Certificate certificate = keyStore.getCertificate(certificateAlias);
        PublicKey publicKey = certificate.getPublicKey();

        log.info("Public key algorithm: {}", publicKey.getAlgorithm());
        log.info("Certificate type: {}", certificate.getType());

        return new KeyPair(publicKey, privateKey);
    }

}
