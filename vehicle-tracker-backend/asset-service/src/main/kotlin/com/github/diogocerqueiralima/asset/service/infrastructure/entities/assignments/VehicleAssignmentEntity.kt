package com.github.diogocerqueiralima.asset.service.infrastructure.entities.assignments

import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.DeviceEntity
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.VehicleEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "vehicle_assignments")
class VehicleAssignmentEntity : AssignmentEntity() {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    lateinit var device: DeviceEntity

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    lateinit var vehicle: VehicleEntity

    @Enumerated(EnumType.STRING)
    @Column(name = "removal_reason", length = 32)
    var removalReason: VehicleRemovalReason? = null

    @Column(name = "installed_by")
    var installedBy: UUID? = null

    @Column(name = "notes", length = 1024)
    var notes: String? = null

}
