package com.github.diogocerqueiralima.domain.authentication.exceptions

/**
 * Thrown when an identity token cannot be decoded into a valid user identity,
 * e.g. because the "sub" claim is missing or is not a valid UUID.
 */
class InvalidIdentityTokenException(message: String) : Exception(message)
