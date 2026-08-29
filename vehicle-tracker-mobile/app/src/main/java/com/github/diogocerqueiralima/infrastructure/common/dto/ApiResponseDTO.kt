package com.github.diogocerqueiralima.infrastructure.common.dto

import kotlinx.serialization.Serializable

/**
 * Data class representing a generic API response with a message and optional data.
 */
@Serializable
data class ApiResponseDTO<T>(val message: String, val data: T?)
