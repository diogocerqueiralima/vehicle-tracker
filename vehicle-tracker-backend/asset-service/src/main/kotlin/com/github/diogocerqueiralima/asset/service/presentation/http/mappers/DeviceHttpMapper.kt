package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.api.common.dto.PageDTO
import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateOrUpdateDeviceRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.DeviceDTO
import java.util.UUID

object DeviceHttpMapper {

    fun toCommand(id: UUID, request: CreateOrUpdateDeviceRequestDTO): CreateOrUpdateDeviceCommand =
        CreateOrUpdateDeviceCommand(
            id,
            request.serialNumber,
            request.model,
            request.manufacturer,
            request.imei,
            request.ownerId
        )

    fun toGetByIdCommand(id: UUID, userId: UUID, isAdmin: Boolean): GetDeviceByIdCommand =
        GetDeviceByIdCommand(id, userId, isAdmin)

    fun toGetPageCommand(pageNumber: Int, pageSize: Int, userId: UUID): GetDevicePageCommand =
        GetDevicePageCommand(pageNumber, pageSize, userId)

    fun toDTO(result: DeviceResult): DeviceDTO =
        DeviceDTO(
            result.id,
            result.createdAt,
            result.updatedAt,
            result.serialNumber,
            result.model,
            result.manufacturer,
            result.imei
        )

    fun toPageDTO(result: PageResult<DeviceResult>): PageDTO<DeviceDTO> =
        PageDTO(
            result.pageNumber,
            result.pageSize,
            result.totalPages,
            result.totalElements,
            result.data.map(::toDTO)
        )

}
