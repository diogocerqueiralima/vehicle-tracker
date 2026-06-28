package com.github.diogocerqueiralima.api.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Generic paginated response payload.")
public record PageDTO<T>(

        @Schema(description = "Current page number using one-based indexing.", example = "1")
        @JsonProperty("page_number") int pageNumber,

        @Schema(description = "Number of items per page.", example = "10")
        @JsonProperty("page_size") int pageSize,

        @Schema(description = "Total number of pages available.", example = "5")
        @JsonProperty("total_pages") int totalPages,

        @Schema(description = "Total number of elements matching the query.", example = "42")
        @JsonProperty("total_elements") long totalElements,

        @Schema(description = "List of items in the current page.")
        @JsonProperty("data") List<T> data

) {}
