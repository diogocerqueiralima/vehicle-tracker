package com.github.diogocerqueiralima.asset.service.presentation.grpc.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentByDeviceIdCommand
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason
import com.github.diogocerqueiralima.schema.proto.DeviceId
import com.github.diogocerqueiralima.schema.proto.VehicleAssignmentResponse
import com.github.diogocerqueiralima.schema.proto.vehicleAssignmentResponse
import java.util.UUID
import com.github.diogocerqueiralima.schema.proto.VehicleRemovalReason as ProtoVehicleRemovalReason

object VehicleAssignmentGrpcMapper {

    fun toGetVehicleAssignmentByDeviceIdCommand(request: DeviceId): GetVehicleAssignmentByDeviceIdCommand =
        GetVehicleAssignmentByDeviceIdCommand(UUID.fromString(request.id))

    fun toResponse(result: VehicleAssignmentResult): VehicleAssignmentResponse =
        vehicleAssignmentResponse {
            vehicleId = result.vehicleId.toString()
            deviceId = result.deviceId.toString()
            assignedAt = result.assignedAt.toProtoTimestamp()
            assignedBy = result.assignedBy.toString()
            active = result.active
            result.unassignedAt?.let { unassignedAt = it.toProtoTimestamp() }
            result.unassignedBy?.let { unassignedBy = it.toString() }
            result.removalReason?.let { removalReason = toProtoRemovalReason(it) }
            result.installedBy?.let { installedBy = it.toString() }
            result.notes?.let { notes = it }
        }

    private fun toProtoRemovalReason(removalReason: VehicleRemovalReason): ProtoVehicleRemovalReason =
        when (removalReason) {
            VehicleRemovalReason.UPGRADE -> ProtoVehicleRemovalReason.VEHICLE_REMOVAL_REASON_UPGRADE
            VehicleRemovalReason.LOSS -> ProtoVehicleRemovalReason.VEHICLE_REMOVAL_REASON_LOSS
            VehicleRemovalReason.SOLD -> ProtoVehicleRemovalReason.VEHICLE_REMOVAL_REASON_SOLD
            VehicleRemovalReason.RETIRED -> ProtoVehicleRemovalReason.VEHICLE_REMOVAL_REASON_RETIRED
            VehicleRemovalReason.OTHER -> ProtoVehicleRemovalReason.VEHICLE_REMOVAL_REASON_OTHER
        }

}
