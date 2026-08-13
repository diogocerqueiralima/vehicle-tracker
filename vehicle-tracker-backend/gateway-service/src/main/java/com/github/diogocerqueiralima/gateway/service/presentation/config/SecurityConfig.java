package com.github.diogocerqueiralima.gateway.service.presentation.config;

import com.github.diogocerqueiralima.gateway.service.presentation.routing.RouteMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive security configuration: validates JWTs (Keycloak-issued) for every request, except
 * routes explicitly marked public in {@code gateway.routes}. This is the single place
 * authentication is enforced for the whole backend, per issue #123 — downstream services no
 * longer need to be reached directly to bypass it.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, RouteMatcher routeMatcher) {

        String[] publicPatterns = routeMatcher.publicPatterns().toArray(String[]::new);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorize -> {
                    if (publicPatterns.length > 0) {
                        authorize.pathMatchers(publicPatterns).permitAll();
                    }
                    authorize.anyExchange().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }

}
