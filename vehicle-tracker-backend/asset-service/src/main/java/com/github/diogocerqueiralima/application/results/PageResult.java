package com.github.diogocerqueiralima.application.results;

import java.util.List;

/**
 * Generic paginated result returned by application use cases.
 *
 * @param pageNumber one-based pageNumber number.
 * @param pageSize pageNumber size requested.
 * @param totalPages total amount of pages.
 * @param totalElements total amount of matched elements.
 * @param data pageNumber content.
 * @param <T> item type.
 */
public record PageResult<T>(
        int pageNumber,
        int pageSize,
        int totalPages,
        long totalElements,
        List<T> data
) {}

