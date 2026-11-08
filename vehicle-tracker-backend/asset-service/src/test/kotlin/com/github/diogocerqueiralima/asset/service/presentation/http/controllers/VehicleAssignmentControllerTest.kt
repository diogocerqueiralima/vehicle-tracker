package com.github.diogocerqueiralima.asset.service.presentation.http.controllers

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentHistoryCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.VehicleAssignmentUseCase
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromVehicleRequestDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class VehicleAssignmentControllerTest {

    @Mock
    private lateinit var vehicleAssignmentUseCase: VehicleAssignmentUseCase

    @InjectMocks
    private lateinit var vehicleAssignmentController: VehicleAssignmentController

    @Test
    @DisplayName("Should assign device to vehicle and return created response")
    fun should_assign_device_to_vehicle_and_return_created_response() {

        val userId = UUID.randomUUID()
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

        whenever(vehicleAssignmentUseCase.assignDeviceToVehicle(any())).thenReturn(result)

        val request = AssignDeviceToVehicleRequestDTO(
            deviceId,
            installedBy,
            "Installed in workshop A"
        )

        val response = vehicleAssignmentController.assignDeviceToVehicle(
            userId.toString(),
            vehicleId,
            request
        )

        val commandCaptor = argumentCaptor<AssignDeviceToVehicleCommand>()
        verify(vehicleAssignmentUseCase).assignDeviceToVehicle(commandCaptor.capture())

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("Device assigned to vehicle successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.deviceId).isEqualTo(deviceId)
        assertThat(body.data.vehicleId).isEqualTo(vehicleId)
        assertThat(body.data.active).isTrue()
        assertThat(commandCaptor.firstValue.assignedBy).isEqualTo(userId)
    }

    @Test
    @DisplayName("Should unassign device from vehicle and return ok response")
    fun should_unassign_device_from_vehicle_and_return_ok_response() {

        val userId = UUID.randomUUID()
        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val assignedAt = Instant.parse("2026-03-20T12:00:00Z")

        val result = VehicleAssignmentResult(
            deviceId,
            vehicleId,
            assignedAt,
            assignedBy,
            Instant.parse("2026-04-01T08:00:00Z"),
            UUID.randomUUID(),
            VehicleRemovalReason.RETIRED,
            null,
            "Uninstalled in workshop B",
            false
        )

        whenever(vehicleAssignmentUseCase.unassignDeviceFromVehicle(any())).thenReturn(result)

        val request = UnassignDeviceFromVehicleRequestDTO(
            deviceId,
            VehicleRemovalReason.RETIRED
        )

        val response = vehicleAssignmentController.unassignDeviceFromVehicle(
            userId.toString(),
            vehicleId,
            request
        )

        val commandCaptor = argumentCaptor<UnassignDeviceFromVehicleCommand>()
        verify(vehicleAssignmentUseCase).unassignDeviceFromVehicle(commandCaptor.capture())

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("Device unassigned from vehicle successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.deviceId).isEqualTo(deviceId)
        assertThat(body.data.vehicleId).isEqualTo(vehicleId)
        assertThat(body.data.unassignedAt).isNotNull()
        assertThat(body.data.unassignedBy).isNotNull()
        assertThat(body.data.removalReason).isEqualTo(VehicleRemovalReason.RETIRED)
        assertThat(body.data.active).isFalse()
        assertThat(commandCaptor.firstValue.unassignedBy).isEqualTo(userId)
    }

    @Test
    @DisplayName("Should return paginated vehicle assignment history with ok response")
    fun should_return_paginated_vehicle_assignment_history_with_ok_response() {

        val userId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val deviceId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val assignedAt = Instant.parse("2026-03-20T12:00:00Z")

        val assignment = VehicleAssignmentResult(
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

        val pageResult = PageResult(1, 10, 1, 1L, listOf(assignment))

        whenever(vehicleAssignmentUseCase.getVehicleAssignmentHistory(any())).thenReturn(pageResult)

        val response = vehicleAssignmentController.getVehicleAssignmentHistory(
            userId.toString(),
            vehicleId,
            1,
            10
        )

        val commandCaptor = argumentCaptor<GetVehicleAssignmentHistoryCommand>()
        verify(vehicleAssignmentUseCase).getVehicleAssignmentHistory(commandCaptor.capture())

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("Vehicle assignment history fetched successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.pageNumber).isEqualTo(1)
        assertThat(body.data.pageSize).isEqualTo(10)
        assertThat(body.data.totalPages).isEqualTo(1)
        assertThat(body.data.totalElements).isEqualTo(1L)
        assertThat(body.data.data).hasSize(1)
        assertThat(body.data.data.first().deviceId).isEqualTo(deviceId)
        assertThat(body.data.data.first().vehicleId).isEqualTo(vehicleId)
        assertThat(commandCaptor.firstValue.vehicleId).isEqualTo(vehicleId)
        assertThat(commandCaptor.firstValue.userId).isEqualTo(userId)
        assertThat(commandCaptor.firstValue.pageNumber).isEqualTo(1)
        assertThat(commandCaptor.firstValue.pageSize).isEqualTo(10)
    }

}
