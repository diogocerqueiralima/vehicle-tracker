package com.github.diogocerqueiralima.gateway.service.domain;

/**
 * Outcome of a rate limit check for a single request.
 *
 * @param allowed         whether the request is allowed to proceed.
 * @param remaining       number of requests still allowed within the current window.
 * @param retryAfterSeconds seconds the client should wait before retrying, only meaningful when {@code allowed} is {@code false}.
 */
public record RateLimitDecision(boolean allowed, long remaining, long retryAfterSeconds) {}
