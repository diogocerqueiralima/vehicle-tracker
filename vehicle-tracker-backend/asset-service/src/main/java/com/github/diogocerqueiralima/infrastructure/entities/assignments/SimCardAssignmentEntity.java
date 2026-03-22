package com.github.diogocerqueiralima.infrastructure.entities.assignments;

import com.github.diogocerqueiralima.domain.assignments.SimCardRemovalReason;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.SimCardEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "sim_card_assignments")
@PrimaryKeyJoinColumn(name = "id")
public class SimCardAssignmentEntity extends AssignmentEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceEntity device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sim_card_iccid", nullable = false)
    private SimCardEntity simCard;

    @Enumerated(EnumType.STRING)
    @Column(name = "removal_reason")
    private SimCardRemovalReason removalReason;

    public SimCardAssignmentEntity() {}

    public DeviceEntity getDevice() {
        return device;
    }

    public void setDevice(DeviceEntity device) {
        this.device = device;
    }

    public SimCardEntity getSimCard() {
        return simCard;
    }

    public void setSimCard(SimCardEntity simCard) {
        this.simCard = simCard;
    }

    public SimCardRemovalReason getRemovalReason() {
        return removalReason;
    }

    public void setRemovalReason(SimCardRemovalReason removalReason) {
        this.removalReason = removalReason;
    }

}

