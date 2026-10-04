package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.api.common.dto.PageDTO
import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentHistoryCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.VehicleAssignmentDTO
import java.util.UUID

object VehicleAssignmentHttpMapper {

    fun toAssignDeviceToVehicleCommand(
        request: AssignDeviceToVehicleRequestDTO, vehicleId: UUID, assignedBy: UUID
    ): AssignDeviceToVehicleCommand =
        AssignDeviceToVehicleCommand(request.deviceId, vehicleId, assignedBy, request.installedBy, request.notes)

    fun toDTO(result: VehicleAssignmentResult): VehicleAssignmentDTO =
        VehicleAssignmentDTO(
            result.deviceId,
            result.vehicleId,
            result.assignedAt,
            result.assignedBy,
            result.unassignedAt,
            result.unassignedBy,
            result.removalReason,
            result.installedBy,
            result.notes,
            result.active
        )

    fun toUnassignDeviceFromVehicleCommand(
        request: UnassignDeviceFromVehicleRequestDTO, vehicleId: UUID, unassignedBy: UUID
    ): UnassignDeviceFromVehicleCommand =
        UnassignDeviceFromVehicleCommand(request.deviceId, vehicleId, unassignedBy, request.removalReason)

    fun toGetVehicleAssignmentHistoryCommand(
        vehicleId: UUID, userId: UUID, pageNumber: Int, pageSize: Int
    ): GetVehicleAssignmentHistoryCommand =
        GetVehicleAssignmentHistoryCommand(vehicleId, userId, pageNumber, pageSize)

    fun toPageDTO(result: PageResult<VehicleAssignmentResult>): PageDTO<VehicleAssignmentDTO> =
        PageDTO(
            result.pageNumber,
            result.pageSize,
            result.totalPages,
            result.totalElements,
            result.data.map(::toDTO)
        )

}
