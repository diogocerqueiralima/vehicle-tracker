package com.github.diogocerqueiralima.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A generic API response wrapper that includes a message and data payload.")
public record ApiResponseDTO<T>(

        @Schema(
                description = """
                        A message providing additional information about the API response,
                        such as success or error details.
                        """
        )
        String message,

        @Schema(
                description = """
                        The data payload of the API response, which can be of any type
                        depending on the specific endpoint and operation.
                        """
        )
        T data

) {}
