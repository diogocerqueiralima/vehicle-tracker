package com.github.diogocerqueiralima.application.results;

import java.util.List;

/**
 * Generic paginated result returned by application use cases.
 *
 * @param pageNumber one-based page number.
 * @param pageSize page size requested.
 * @param totalElements total amount of matched elements.
 * @param totalPages total amount of pages.
 * @param data page content.
 * @param <T> item type.
 */
public record PageResult<T>(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        List<T> data
) {}

