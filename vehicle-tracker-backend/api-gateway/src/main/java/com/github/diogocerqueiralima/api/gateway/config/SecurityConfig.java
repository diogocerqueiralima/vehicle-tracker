package com.github.diogocerqueiralima.api.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Configuration class for setting up security settings.
 */
@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String SWAGGER_UI_HTML = "/swagger-ui.html";
    private static final String SWAGGER_API_DOCS = "/v3/api-docs/**";
    private static final String SWAGGER_UI = "/swagger-ui/**";

    /**
     *
     * Configures the default security filter chain for the application. Route-scoped rules
     * (see {@code gateway.routes[].rules} in application.yml) require the given role; every
     * other request under a configured route's prefix just requires authentication, and each
     * route's swagger endpoints are open.
     *
     * @param http the {@link ServerHttpSecurity} object used to configure security settings
     * @param routes the configured downstream services and their authorization rules
     * @return the {@link SecurityWebFilterChain} representing the configured security filter chain
     */
    @Bean
    public SecurityWebFilterChain defaultSecurityFilterChain(ServerHttpSecurity http, GatewayRoutesProperties routes) {
        return http
                .authorizeExchange(authorize -> {

                    for (GatewayRoutesProperties.Route route : routes.routes()) {

                        for (GatewayRoutesProperties.Route.Rule rule : route.rules()) {

                            String path = route.prefix() + rule.path();

                            log.info("Registering gateway rule: {} {} requires role {}", rule.method(), path, rule.role());

                            authorize.pathMatchers(HttpMethod.valueOf(rule.method()), path)
                                    .hasRole(rule.role());
                        }

                        authorize.pathMatchers(
                                route.prefix() + SWAGGER_UI_HTML,
                                route.prefix() + SWAGGER_API_DOCS,
                                route.prefix() + SWAGGER_UI
                        ).permitAll();
                    }

                    authorize.anyExchange().authenticated();
                })
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    /**
     *
     * Configures a JWT authentication converter that extracts roles from the JWT token and converts them into Spring Security authorities.
     *
     * @return a {@link Converter} that converts a {@link Jwt} into a reactive stream of an authentication token with granted authorities
     */
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Map<String, Object> realmAccess = jwt.getClaim("resource_access");

            if (realmAccess != null && realmAccess.get("tracker") instanceof Map<?, ?> resource) {

                Object roles = resource.get("roles");

                if (roles instanceof Collection<?> r) {
                    authorities.addAll(
                            r.stream()
                                    .filter(String.class::isInstance)
                                    .map(String.class::cast)
                                    .map(role -> "ROLE_" + role.toUpperCase())
                                    .map(SimpleGrantedAuthority::new)
                                    .toList()
                    );
                }

            }

            log.info("JWT sub={} resolved authorities: {}", jwt.getSubject(), authorities);

            return authorities;
        }

    }

}
