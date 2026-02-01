package com.github.diogocerqueiralima.presentation.config;

import com.github.diogocerqueiralima.presentation.filters.CertificateFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CertificateFilter certificateFilter;

    public SecurityConfig(CertificateFilter certificateFilter) {
        this.certificateFilter = certificateFilter;
    }

    /**
     *
     * Security filter chain for bootstrap certificate endpoints.
     * Only authenticated requests with valid JWT tokens are allowed.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     */
    @Bean
    @Order(1)
    public SecurityFilterChain bootstrapCertificateSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher(ApplicationURIs.BOOTSTRAP_CERTIFICATE_BASE_URI + "/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }

    /**
     *
     * Security filter chain for certificate endpoints.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     */
    @Bean
    @Order(2)
    public SecurityFilterChain certificateSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher(ApplicationURIs.CERTIFICATE_BASE_URI + "/**")
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(certificateFilter, AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .build();
    }

    /**
     *
     * Default security filter chain for all other endpoints.
     * All requests require authentication with valid JWT tokens.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(Customizer.withDefaults())
                )
                .build();
    }

}
