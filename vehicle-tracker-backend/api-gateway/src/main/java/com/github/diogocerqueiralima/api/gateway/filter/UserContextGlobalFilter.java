package com.github.diogocerqueiralima.api.gateway.filter;

import com.github.diogocerqueiralima.api.common.headers.ReservedHeaders;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Strips any inbound user-context headers from clients and, once the request is authenticated,
 * re-injects them from the validated JWT so downstream services can trust them without
 * parsing the token themselves.
 */
@Component
@NullMarked
public class UserContextGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(UserContextGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest strippedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(ReservedHeaders.USER_ID);
                    headers.remove(ReservedHeaders.USER_ROLES);
                    headers.remove(ReservedHeaders.USER_USERNAME);
                })
                .build();

        ServerWebExchange strippedExchange = exchange.mutate()
                .request(strippedRequest)
                .build();

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> Mono.justOrEmpty(context.getAuthentication()))
                .flatMap(authentication -> {

                    if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
                        return chain.filter(strippedExchange);
                    }

                    Jwt jwt = jwtAuth.getToken();
                    String roles = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .filter(Objects::nonNull)
                            .map(authority -> authority.replaceFirst("^ROLE_", ""))
                            .collect(Collectors.joining(","));

                    log.info("Injecting user context for {} {} -> userId={}, roles=[{}]",
                            exchange.getRequest().getMethod(), exchange.getRequest().getPath(), jwt.getSubject(), roles);

                    ServerHttpRequest enrichedRequest = strippedExchange.getRequest().mutate()
                            .header(ReservedHeaders.USER_ID, jwt.getSubject())
                            .header(ReservedHeaders.USER_USERNAME, Objects.requireNonNullElse(jwt.getClaimAsString("preferred_username"), ""))
                            .header(ReservedHeaders.USER_ROLES, roles)
                            .build();

                    return chain.filter(strippedExchange.mutate().request(enrichedRequest).build());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No JwtAuthenticationToken in security context for {} {} -> forwarding without user context headers",
                            exchange.getRequest().getMethod(), exchange.getRequest().getPath());
                    return chain.filter(strippedExchange);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
