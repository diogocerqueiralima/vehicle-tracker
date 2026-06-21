package com.github.diogocerqueiralima.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Generic paginated payload returned by HTTP endpoints.
 *
 * @param pageNumber one-based pageNumber number.
 * @param pageSize pageNumber size requested.
 * @param totalPages total amount of pages.
 * @param totalElements total amount of matched elements.
 * @param data pageNumber content.
 * @param <T> item type.
 */
public record PageDTO<T>(
        @JsonProperty("page_number") int pageNumber,
        @JsonProperty("page_size") int pageSize,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("data") List<T> data
) {}

