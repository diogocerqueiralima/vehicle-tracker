package com.github.diogocerqueiralima.identity.service.infrastructure.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table
import java.math.BigInteger
import java.time.Instant

@Entity
@Table(name = "certificates")
@Inheritance(strategy = InheritanceType.JOINED)
class CertificateEntity {

    @Id
    @Column(name = "serial_number")
    lateinit var serialNumber: BigInteger

    @Column(nullable = false)
    lateinit var subject: String

    @Column(name = "issued_at", nullable = false)
    lateinit var issuedAt: Instant

    @Column(name = "expires_at", nullable = false)
    lateinit var expiresAt: Instant

    @Column(nullable = false)
    var revoked: Boolean = false

}
