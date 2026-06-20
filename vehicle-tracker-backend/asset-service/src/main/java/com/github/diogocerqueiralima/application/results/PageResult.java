package com.github.diogocerqueiralima.application.results;

import java.util.List;

/**
 * Generic paginated result returned by application use cases.
 *
 * @param pageNumber one-based pageNumber number.
 * @param pageSize pageNumber size requested.
 * @param totalElements total amount of matched elements.
 * @param totalPages total amount of pages.
 * @param data pageNumber content.
 * @param <T> item type.
 */
public record PageResult<T>(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        List<T> data
) {}

