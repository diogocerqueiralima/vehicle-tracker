package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.api.common.dto.PageDTO
import com.github.diogocerqueiralima.asset.service.application.commands.CreateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehiclePageCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleResult
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.VehicleDTO
import java.util.UUID

object VehicleHttpMapper {

    fun toCreateCommand(request: CreateVehicleRequestDTO, userId: UUID): CreateVehicleCommand =
        CreateVehicleCommand(
            request.vin,
            request.plate,
            request.model,
            request.manufacturer,
            request.manufacturingDate,
            userId
        )

    fun toUpdateCommand(id: UUID, request: UpdateVehicleRequestDTO, userId: UUID): UpdateVehicleCommand =
        UpdateVehicleCommand(
            id,
            request.vin,
            request.plate,
            request.model,
            request.manufacturer,
            request.manufacturingDate,
            userId
        )

    fun toGetByIdCommand(id: UUID, userId: UUID): GetVehicleByIdCommand =
        GetVehicleByIdCommand(id, userId)

    fun toGetPageCommand(pageNumber: Int, pageSize: Int, userId: UUID): GetVehiclePageCommand =
        GetVehiclePageCommand(pageNumber, pageSize, userId)

    fun toDTO(result: VehicleResult): VehicleDTO =
        VehicleDTO(
            result.id,
            result.createdAt,
            result.updatedAt,
            result.vin,
            result.plate,
            result.model,
            result.manufacturer,
            result.manufacturingDate
        )

    fun toPageDTO(result: PageResult<VehicleResult>): PageDTO<VehicleDTO> =
        PageDTO(
            result.pageNumber,
            result.pageSize,
            result.totalPages,
            result.totalElements,
            result.data.map(::toDTO)
        )

}
