package com.github.diogocerqueiralima.infrastructure.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a paginated response with page number, page size, total pages, and a list of data items.
 */
@Serializable
data class PageDTO<T>(
    @SerialName("page_number") val pageNumber: Int,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_elements") val totalElements: Int,
    @SerialName("data") val data: List<T>
)
