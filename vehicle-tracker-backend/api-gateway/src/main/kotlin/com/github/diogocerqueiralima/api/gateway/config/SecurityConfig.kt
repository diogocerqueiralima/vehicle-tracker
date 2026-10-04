package com.github.diogocerqueiralima.api.gateway.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain

private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

private const val SWAGGER_UI_HTML = "/swagger-ui.html"
private const val SWAGGER_API_DOCS = "/v3/api-docs/**"
private const val SWAGGER_UI = "/swagger-ui/**"

/**
 * Configuration class for setting up security settings.
 */
@EnableWebFluxSecurity
@Configuration
class SecurityConfig {

    /**
     * Configures the default security filter chain for the application. Route-scoped rules
     * (see `gateway.routes[].rules` in application.yml) require the given role; every
     * other request under a configured route's prefix just requires authentication, and each
     * route's swagger endpoints are open.
     *
     * @param http the [ServerHttpSecurity] object used to configure security settings
     * @param routes the configured downstream services and their authorization rules
     * @return the [SecurityWebFilterChain] representing the configured security filter chain
     */
    @Bean
    fun defaultSecurityFilterChain(http: ServerHttpSecurity, routes: GatewayRoutesProperties): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .authorizeExchange { authorize ->

                routes.routes.forEach { route ->

                    route.rules.forEach { rule ->

                        val path = route.prefix + rule.path

                        log.info("Registering gateway rule: {} {} requires role {}", rule.method, path, rule.role)

                        authorize.pathMatchers(HttpMethod.valueOf(rule.method), path)
                            .hasRole(rule.role)
                    }

                    authorize.pathMatchers(
                        route.prefix + SWAGGER_UI_HTML,
                        route.prefix + SWAGGER_API_DOCS,
                        route.prefix + SWAGGER_UI
                    ).permitAll()
                }

                authorize.anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth ->
                oauth.jwt { it.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
            }
            .build()

    /**
     * Configures a JWT authentication converter that extracts roles from the JWT token and converts them into Spring Security authorities.
     *
     * @return a converter that turns a [Jwt] into a reactive stream of an authentication token with granted authorities
     */
    @Bean
    fun jwtAuthenticationConverter(): ReactiveJwtAuthenticationConverterAdapter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(KeycloakRealmRoleConverter())
        return ReactiveJwtAuthenticationConverterAdapter(converter)
    }

    /**
     * Maps the roles of the `tracker` client in the `resource_access` claim to `ROLE_*` authorities.
     */
    internal class KeycloakRealmRoleConverter : Converter<Jwt, Collection<GrantedAuthority>> {

        override fun convert(jwt: Jwt): Collection<GrantedAuthority> {

            // 1. Read the roles of the "tracker" client from the resource_access claim
            val resource = jwt.getClaim<Map<*, *>>("resource_access")?.get("tracker") as? Map<*, *>
            val roles = resource?.get("roles") as? Collection<*> ?: emptyList<Any>()

            // 2. Keep only string roles and prefix them as Spring Security authorities
            val authorities = roles
                .filterIsInstance<String>()
                .map { SimpleGrantedAuthority("ROLE_${it.uppercase()}") }

            log.info("JWT sub={} resolved authorities: {}", jwt.subject, authorities)

            return authorities
        }

    }

}
