package com.github.diogocerqueiralima.api.gateway.filter

import com.github.diogocerqueiralima.api.common.headers.ReservedHeaders
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

private val log = LoggerFactory.getLogger(UserContextGlobalFilter::class.java)

/**
 * Strips any inbound user-context headers from clients and, once the request is authenticated,
 * re-injects them from the validated JWT so downstream services can trust them without
 * parsing the token themselves.
 */
@Component
class UserContextGlobalFilter : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {

        // 1. Strip any user-context headers sent by the client
        val strippedRequest = exchange.request.mutate()
            .headers { headers ->
                headers.remove(ReservedHeaders.USER_ID)
                headers.remove(ReservedHeaders.USER_ROLES)
                headers.remove(ReservedHeaders.USER_USERNAME)
            }
            .build()

        val strippedExchange = exchange.mutate()
            .request(strippedRequest)
            .build()

        // 2. Re-inject them from the validated JWT, if there is one
        return ReactiveSecurityContextHolder.getContext()
            .flatMap { Mono.justOrEmpty(it.authentication) }
            .flatMap { authentication ->

                if (authentication !is JwtAuthenticationToken) {
                    return@flatMap chain.filter(strippedExchange)
                }

                val jwt = authentication.token
                val roles = authentication.authorities
                    .mapNotNull { it.authority }
                    .joinToString(",") { it.replaceFirst(ROLE_PREFIX, "") }

                log.info(
                    "Injecting user context for {} {} -> userId={}, roles=[{}]",
                    exchange.request.method, exchange.request.path, jwt.subject, roles
                )

                val enrichedRequest = strippedExchange.request.mutate()
                    .header(ReservedHeaders.USER_ID, jwt.subject)
                    .header(ReservedHeaders.USER_USERNAME, jwt.getClaimAsString("preferred_username") ?: "")
                    .header(ReservedHeaders.USER_ROLES, roles)
                    .build()

                chain.filter(strippedExchange.mutate().request(enrichedRequest).build())
            }
            .switchIfEmpty(Mono.defer {
                log.warn(
                    "No JwtAuthenticationToken in security context for {} {} -> forwarding without user context headers",
                    exchange.request.method, exchange.request.path
                )
                chain.filter(strippedExchange)
            })
    }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    private companion object {
        val ROLE_PREFIX = Regex("^ROLE_")
    }

}
