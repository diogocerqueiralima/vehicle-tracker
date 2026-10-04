package com.github.diogocerqueiralima.api.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Binds the downstream services configured under `gateway.routes` in application.yml.
 *
 * @property routes the configured downstream services
 */
@ConfigurationProperties(prefix = "gateway")
data class GatewayRoutesProperties(val routes: List<Route> = emptyList()) {

    /**
     * A single downstream service route.
     *
     * @property id unique route id
     * @property prefix path prefix clients use to reach the service (e.g. `/assets`)
     * @property uri downstream service base URI (e.g. `http://asset-service:8080`)
     * @property rules method/path combinations under this route that require a specific role; anything not
     * listed just requires authentication
     */
    data class Route(
        val id: String,
        val prefix: String,
        val uri: String,
        val rules: List<Rule> = emptyList()
    ) {

        /**
         * An authorization rule scoped to a route.
         *
         * @property method HTTP method the rule applies to (e.g. `POST`)
         * @property path path, relative to the route's prefix, the rule applies to (e.g. `/devices`)
         * @property role role required to access the method/path, without the `ROLE_` prefix (e.g. `ADMIN`)
         */
        data class Rule(val method: String, val path: String, val role: String)

    }

}
