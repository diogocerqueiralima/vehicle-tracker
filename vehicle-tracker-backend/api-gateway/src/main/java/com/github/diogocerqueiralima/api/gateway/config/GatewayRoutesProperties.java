package com.github.diogocerqueiralima.api.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Binds the downstream services configured under {@code gateway.routes} in application.yml.
 *
 * @param routes the configured downstream services
 */
@ConfigurationProperties(prefix = "gateway")
public record GatewayRoutesProperties(List<Route> routes) {

    /**
     * A single downstream service route.
     *
     * @param id     unique route id
     * @param prefix path prefix clients use to reach the service (e.g. {@code /assets})
     * @param uri    downstream service base URI (e.g. {@code http://asset-service:8080})
     * @param rules  method/path combinations under this route that require a specific role; anything not
     *               listed just requires authentication
     */
    public record Route(String id, String prefix, String uri, List<Rule> rules) {

        public List<Rule> rules() {
            return rules == null ? List.of() : rules;
        }

        /**
         * An authorization rule scoped to a route.
         *
         * @param method HTTP method the rule applies to (e.g. {@code POST})
         * @param path   path, relative to the route's prefix, the rule applies to (e.g. {@code /devices})
         * @param role   role required to access the method/path, without the {@code ROLE_} prefix (e.g. {@code ADMIN})
         */
        public record Rule(String method, String path, String role) {}

    }

}
