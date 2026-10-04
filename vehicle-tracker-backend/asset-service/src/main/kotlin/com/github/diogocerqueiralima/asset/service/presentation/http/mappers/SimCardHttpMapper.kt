package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.DeleteSimCardByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetSimCardByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.SimCardDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateSimCardRequestDTO
import java.util.UUID

object SimCardHttpMapper {

    fun toCreateCommand(request: CreateSimCardRequestDTO, userId: UUID): CreateSimCardCommand =
        CreateSimCardCommand(request.iccid, request.msisdn, request.imsi, userId)

    fun toUpdateCommand(id: UUID, request: UpdateSimCardRequestDTO, userId: UUID): UpdateSimCardCommand =
        UpdateSimCardCommand(id, request.iccid, request.msisdn, request.imsi, userId)

    fun toGetByIdCommand(id: UUID, userId: UUID): GetSimCardByIdCommand =
        GetSimCardByIdCommand(id, userId)

    fun toDeleteByIdCommand(id: UUID, userId: UUID): DeleteSimCardByIdCommand =
        DeleteSimCardByIdCommand(id, userId)

    fun toDTO(result: SimCardResult): SimCardDTO =
        SimCardDTO(result.id, result.createdAt, result.updatedAt, result.iccid, result.msisdn, result.imsi)

}
