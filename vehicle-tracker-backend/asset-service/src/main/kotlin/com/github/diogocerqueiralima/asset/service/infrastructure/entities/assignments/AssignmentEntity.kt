package com.github.diogocerqueiralima.asset.service.infrastructure.entities.assignments

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.time.Instant
import java.util.UUID

@MappedSuperclass
abstract class AssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    var id: Long? = null

    @Column(name = "assigned_at", nullable = false, updatable = false)
    lateinit var assignedAt: Instant

    @Column(name = "unassigned_at")
    var unassignedAt: Instant? = null

    @Column(name = "assigned_by", nullable = false, updatable = false)
    lateinit var assignedBy: UUID

    @Column(name = "unassigned_by")
    var unassignedBy: UUID? = null

}
