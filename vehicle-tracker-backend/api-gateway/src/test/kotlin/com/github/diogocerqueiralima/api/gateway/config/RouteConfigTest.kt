package com.github.diogocerqueiralima.api.gateway.config

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.support.GenericApplicationContext
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import reactor.core.publisher.Mono

class RouteConfigTest {

    private fun locator(vararg routes: GatewayRoutesProperties.Route): RouteLocator {
        val context = GenericApplicationContext()
        context.registerBean(PathRoutePredicateFactory::class.java, java.util.function.Supplier { PathRoutePredicateFactory(WebFluxProperties()) })
        context.refresh()
        return RouteConfig().routeLocator(RouteLocatorBuilder(context), GatewayRoutesProperties(routes.toList()))
    }

    private val asset = GatewayRoutesProperties.Route("asset-service", "/assets", "http://asset:8080")
    private val identity = GatewayRoutesProperties.Route("identity-service", "/identity", "http://identity:8081")

    @Test
    @DisplayName("Should create one route per configured service forwarding to its uri")
    fun `should create one route per service`() {
        run {
            val routes = locator(asset, identity).routes.collectList().block()!!

            assertEquals(setOf("asset-service", "identity-service"), routes.map { it.id }.toSet())
            assertEquals("http://asset:8080", routes.first { it.id == "asset-service" }.uri.toString())
            assertEquals("http://identity:8081", routes.first { it.id == "identity-service" }.uri.toString())
        }
    }

    @Test
    @DisplayName("Should match requests under the prefix and reject other paths")
    fun `should match only paths under prefix`() {
        run {
            val route = locator(asset, identity).routes.collectList().block()!!
                .first { it.id == "asset-service" }

            fun matches(path: String) = route.predicate.apply(
                MockServerWebExchange.from(MockServerHttpRequest.get(path))
            ).let { Mono.from(it).block() == true }

            assertTrue(matches("/assets/devices/1"))
            assertTrue(!matches("/identity/certificates"))
            assertTrue(!matches("/other"))
        }
    }

    @Test
    @DisplayName("Should create no routes when none are configured")
    fun `should create no routes when empty`() {
        run {
            val routes = locator().routes.collectList().block()!!
            assertTrue(routes.isEmpty())
        }
    }

}
