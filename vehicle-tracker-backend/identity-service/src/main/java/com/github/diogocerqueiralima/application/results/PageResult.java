package com.github.diogocerqueiralima.application.results;

import java.util.List;

public record PageResult<T>(int currentPage, int totalPages, long totalItems, List<T> items) {}
