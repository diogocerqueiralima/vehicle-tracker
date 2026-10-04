package com.github.diogocerqueiralima.identity.service.infrastructure.config

import jakarta.annotation.PostConstruct
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.KeyPair
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.PrivateKey
import java.security.Security

private val log = LoggerFactory.getLogger(KeyStoreConfig::class.java)

@Configuration
class KeyStoreConfig(
    @Value("\${keystore.pkcs11.config}") private val pkcs11Config: String,
    @Value("\${keystore.pkcs11.pin}") private val pkcs11Pin: String,
    @Value("\${keystore.certificate.alias}") private val certificateAlias: String,
    @Value("\${keystore.key.alias}") private val keyAlias: String,
    @Value("\${keystore.key.password}") private val keyPassword: String
) {

    @PostConstruct
    fun init() {

        Security.addProvider(
            Security.getProvider("SunPKCS11")
                .configure(pkcs11Config)
        )

        Security.addProvider(BouncyCastleProvider())
    }

    @Bean
    fun keyStore(): KeyStore {

        val keyStore = KeyStore.getInstance("PKCS11")
        keyStore.load(null, pkcs11Pin.toCharArray())

        return keyStore
    }

    @Bean
    fun keyPair(keyStore: KeyStore): KeyPair {

        val privateKey = keyStore.getKey(keyAlias, keyPassword.toCharArray()) as? PrivateKey
            ?: throw KeyStoreException("The key retrieved is not a private key")

        log.info("Private key algorithm: {}", privateKey.algorithm)

        val certificate = keyStore.getCertificate(certificateAlias)
            ?: throw KeyStoreException("Certificate with alias $certificateAlias not found")
        val publicKey = certificate.publicKey

        log.info("Public key algorithm: {}", publicKey.algorithm)
        log.info("Certificate type: {}", certificate.type)

        return KeyPair(publicKey, privateKey)
    }

}
