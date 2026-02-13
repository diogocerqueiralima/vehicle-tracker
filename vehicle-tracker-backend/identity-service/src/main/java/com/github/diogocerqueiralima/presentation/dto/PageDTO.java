package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        description = """
                A generic DTO representing a paginated response, including the current page number, total pages,
                and a list of content items.
                """
)
public record PageDTO<T>(

        @Schema(description = "The current page number (1-based index).")
        int page,

        @Schema(description = "The total number of pages available based on the pagination parameters.")
        @JsonProperty("total_pages")
        int totalPages,

        @Schema(description = "The list of content items for the current page.")
        List<T> content

) {}
