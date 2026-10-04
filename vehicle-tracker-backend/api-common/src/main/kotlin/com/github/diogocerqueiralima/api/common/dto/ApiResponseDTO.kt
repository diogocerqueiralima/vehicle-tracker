package com.github.diogocerqueiralima.api.common.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A generic API response wrapper that includes a message and items payload.")
data class ApiResponseDTO<T>(

    @field:Schema(
        description = "A message providing additional information about the API response, " +
                "such as success or error details."
    )
    val message: String,

    @field:Schema(
        description = "The items payload of the API response, which can be of any type " +
                "depending on the specific endpoint and operation."
    )
    val data: T

)
