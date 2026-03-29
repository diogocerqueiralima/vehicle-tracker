package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Generic paginated payload returned by HTTP endpoints.
 *
 * @param pageNumber one-based page number.
 * @param pageSize page size requested.
 * @param totalElements total amount of matched elements.
 * @param totalPages total amount of pages.
 * @param data page content.
 * @param <T> item type.
 */
public record PageDTO<T>(
        @JsonProperty("page_number") int pageNumber,
        @JsonProperty("page_size") int pageSize,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("data") List<T> data
) {}

