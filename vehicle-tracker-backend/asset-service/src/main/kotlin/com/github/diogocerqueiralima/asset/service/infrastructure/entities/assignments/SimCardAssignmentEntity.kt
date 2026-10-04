package com.github.diogocerqueiralima.asset.service.infrastructure.entities.assignments

import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardRemovalReason
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.DeviceEntity
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.SimCardEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "sim_card_assignments")
class SimCardAssignmentEntity : AssignmentEntity() {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    lateinit var device: DeviceEntity

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sim_card_id", nullable = false)
    lateinit var simCard: SimCardEntity

    @Enumerated(EnumType.STRING)
    @Column(name = "removal_reason")
    var removalReason: SimCardRemovalReason? = null

}
