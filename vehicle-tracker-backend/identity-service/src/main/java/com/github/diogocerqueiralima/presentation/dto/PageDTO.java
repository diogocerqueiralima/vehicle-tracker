package com.github.diogocerqueiralima.presentation.dto;

import java.util.List;

public record PageDTO<T>(int page, int totalPages, List<T> content) {}
