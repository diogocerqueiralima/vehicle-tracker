package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromVehicleRequestDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class VehicleAssignmentHttpMapperTest {

    @Test
    @DisplayName("Should map assign request to command")
    fun should_map_assign_request_to_command() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val installedBy = UUID.randomUUID()

        val request = AssignDeviceToVehicleRequestDTO(
            deviceId,
            installedBy,
            "Installed in workshop A"
        )

        val command = VehicleAssignmentHttpMapper.toAssignDeviceToVehicleCommand(
            request, vehicleId, assignedBy
        )

        assertThat(command.deviceId).isEqualTo(deviceId)
        assertThat(command.vehicleId).isEqualTo(vehicleId)
        assertThat(command.assignedBy).isEqualTo(assignedBy)
        assertThat(command.installedBy).isEqualTo(installedBy)
        assertThat(command.notes).isEqualTo("Installed in workshop A")
    }

    @Test
    @DisplayName("Should map assignment result to dto")
    fun should_map_assignment_result_to_dto() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val installedBy = UUID.randomUUID()
        val assignedAt = Instant.parse("2026-03-20T12:00:00Z")

        val result = VehicleAssignmentResult(
            deviceId,
            vehicleId,
            assignedAt,
            assignedBy,
            null,
            null,
            null,
            installedBy,
            "Installed in workshop A",
            true
        )

        val dto = VehicleAssignmentHttpMapper.toDTO(result)

        assertThat(dto.deviceId).isEqualTo(deviceId)
        assertThat(dto.vehicleId).isEqualTo(vehicleId)
        assertThat(dto.assignedAt).isEqualTo(assignedAt)
        assertThat(dto.assignedBy).isEqualTo(assignedBy)
        assertThat(dto.unassignedAt).isNull()
        assertThat(dto.unassignedBy).isNull()
        assertThat(dto.removalReason).isNull()
        assertThat(dto.installedBy).isEqualTo(installedBy)
        assertThat(dto.notes).isEqualTo("Installed in workshop A")
        assertThat(dto.active).isTrue()
    }

    @Test
    @DisplayName("Should map unassign request to command")
    fun should_map_unassign_request_to_command() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val unassignedBy = UUID.randomUUID()

        val request = UnassignDeviceFromVehicleRequestDTO(
            deviceId,
            VehicleRemovalReason.LOSS
        )

        val command = VehicleAssignmentHttpMapper.toUnassignDeviceFromVehicleCommand(
            request,
            vehicleId,
            unassignedBy
        )

        assertThat(command.deviceId).isEqualTo(deviceId)
        assertThat(command.vehicleId).isEqualTo(vehicleId)
        assertThat(command.unassignedBy).isEqualTo(unassignedBy)
        assertThat(command.removalReason).isEqualTo(VehicleRemovalReason.LOSS)
    }

    @Test
    @DisplayName("Should map vehicle id, user id and pagination params to get history command")
    fun should_map_vehicle_id_user_id_and_pagination_params_to_get_history_command() {

        val vehicleId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val command = VehicleAssignmentHttpMapper.toGetVehicleAssignmentHistoryCommand(
            vehicleId, userId, 2, 20
        )

        assertThat(command.vehicleId).isEqualTo(vehicleId)
        assertThat(command.userId).isEqualTo(userId)
        assertThat(command.pageNumber).isEqualTo(2)
        assertThat(command.pageSize).isEqualTo(20)
    }

    @Test
    @DisplayName("Should map page result to page dto with mapped assignment dtos")
    fun should_map_page_result_to_page_dto_with_mapped_assignment_dtos() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val assignedAt = Instant.parse("2026-03-20T12:00:00Z")

        val result = VehicleAssignmentResult(
            deviceId,
            vehicleId,
            assignedAt,
            assignedBy,
            null,
            null,
            null,
            null,
            null,
            true
        )

        val pageResult = PageResult(1, 10, 3, 25L, listOf(result))

        val dto = VehicleAssignmentHttpMapper.toPageDTO(pageResult)

        assertThat(dto.pageNumber).isEqualTo(1)
        assertThat(dto.pageSize).isEqualTo(10)
        assertThat(dto.totalPages).isEqualTo(3)
        assertThat(dto.totalElements).isEqualTo(25L)
        assertThat(dto.data).hasSize(1)
        assertThat(dto.data.first().deviceId).isEqualTo(deviceId)
        assertThat(dto.data.first().vehicleId).isEqualTo(vehicleId)
        assertThat(dto.data.first().assignedAt).isEqualTo(assignedAt)
        assertThat(dto.data.first().assignedBy).isEqualTo(assignedBy)
        assertThat(dto.data.first().active).isTrue()
    }

}
