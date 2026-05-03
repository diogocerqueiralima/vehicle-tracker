package com.github.diogocerqueiralima.presentation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.DEVICES_BASE_URI;
import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.DEVICES_ID_URI;

/**
 * Configuration class for Spring Security, setting up authentication and authorization for the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     *
     * Default security filter chain configuration that requires authentication for all requests and sets up JWT-based OAuth2 resource server support.
     *
     * @param http the {@link HttpSecurity} object to configure the security settings.
     * @return the configured {@link SecurityFilterChain}.
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, DEVICES_BASE_URI).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, DEVICES_ID_URI).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
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

            return authorities;
        }
    }

}
