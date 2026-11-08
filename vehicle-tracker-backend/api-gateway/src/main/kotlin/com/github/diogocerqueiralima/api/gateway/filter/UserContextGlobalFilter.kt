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

        // 2. Build the exchange to forward: enriched when there is a JWT, stripped otherwise
        return ReactiveSecurityContextHolder.getContext()
            .flatMap { Mono.justOrEmpty(it.authentication as? JwtAuthenticationToken) }
            .map { withUserContext(strippedExchange, it) }
            .switchIfEmpty(Mono.fromSupplier {
                log.warn(
                    "No JwtAuthenticationToken in security context for {} {} -> forwarding without user context headers",
                    exchange.request.method, exchange.request.path
                )
                strippedExchange
            })
            // 3. Single call to the chain, so it is invoked exactly once on every path
            .flatMap { chain.filter(it) }
    }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    /** Re-injects the user context headers from the validated [authentication] token. */
    private fun withUserContext(exchange: ServerWebExchange, authentication: JwtAuthenticationToken): ServerWebExchange {

        val jwt = authentication.token
        val roles = authentication.authorities
            .mapNotNull { it.authority }
            .joinToString(",") { it.replaceFirst(ROLE_PREFIX, "") }

        log.info(
            "Injecting user context for {} {} -> userId={}, roles=[{}]",
            exchange.request.method, exchange.request.path, jwt.subject, roles
        )

        val enrichedRequest = exchange.request.mutate()
            .header(ReservedHeaders.USER_ID, jwt.subject)
            .header(ReservedHeaders.USER_USERNAME, jwt.getClaimAsString("preferred_username") ?: "")
            .header(ReservedHeaders.USER_ROLES, roles)
            .build()

        return exchange.mutate().request(enrichedRequest).build()
    }

    private companion object {
        val ROLE_PREFIX = Regex("^ROLE_")
    }

}
