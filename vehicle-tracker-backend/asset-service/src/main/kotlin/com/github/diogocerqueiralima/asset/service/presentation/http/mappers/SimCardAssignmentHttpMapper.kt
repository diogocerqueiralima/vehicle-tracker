package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.results.SimCardAssignmentResult
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.SimCardAssignmentDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromSimCardRequestDTO
import java.util.UUID

object SimCardAssignmentHttpMapper {

    fun toAssignDeviceToSimCardCommand(
        request: AssignDeviceToSimCardRequestDTO, simCardId: UUID, assignedBy: UUID
    ): AssignDeviceToSimCardCommand =
        AssignDeviceToSimCardCommand(request.deviceId, simCardId, assignedBy)

    fun toDTO(result: SimCardAssignmentResult): SimCardAssignmentDTO =
        SimCardAssignmentDTO(
            result.deviceId,
            result.simCardId,
            result.assignedAt,
            result.assignedBy,
            result.unassignedAt,
            result.unassignedBy,
            result.removalReason,
            result.active
        )

    fun toUnassignDeviceFromSimCardCommand(
        request: UnassignDeviceFromSimCardRequestDTO, simCardId: UUID, unassignedBy: UUID
    ): UnassignDeviceFromSimCardCommand =
        UnassignDeviceFromSimCardCommand(request.deviceId, simCardId, unassignedBy, request.removalReason)

}
