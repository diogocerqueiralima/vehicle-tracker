package com.github.diogocerqueiralima.asset.service.application.results

/**
 * Generic paginated result returned by application use cases.
 *
 * @param T item type.
 * @property pageNumber one-based page number.
 * @property pageSize page size requested.
 * @property totalPages total amount of pages.
 * @property totalElements total amount of matched elements.
 * @property data page content.
 */
data class PageResult<T>(
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalElements: Long,
    val data: List<T>
)
