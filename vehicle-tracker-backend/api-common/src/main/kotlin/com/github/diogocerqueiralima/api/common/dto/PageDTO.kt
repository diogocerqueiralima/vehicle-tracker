package com.github.diogocerqueiralima.api.common.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Generic paginated response payload.")
data class PageDTO<T>(

    @field:Schema(description = "Current page number using one-based indexing.", example = "1")
    @JsonProperty("page_number") val pageNumber: Int,

    @field:Schema(description = "Number of items per page.", example = "10")
    @JsonProperty("page_size") val pageSize: Int,

    @field:Schema(description = "Total number of pages available.", example = "5")
    @JsonProperty("total_pages") val totalPages: Int,

    @field:Schema(description = "Total number of elements matching the query.", example = "42")
    @JsonProperty("total_elements") val totalElements: Long,

    @field:Schema(description = "List of items in the current page.")
    @JsonProperty("data") val data: List<T>

)
