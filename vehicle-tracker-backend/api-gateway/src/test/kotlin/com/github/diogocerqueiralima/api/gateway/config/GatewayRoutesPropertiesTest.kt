package com.github.diogocerqueiralima.api.gateway.config

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class GatewayRoutesPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(GatewayRoutesProperties::class)
    class TestConfig

    private val runner = ApplicationContextRunner().withUserConfiguration(TestConfig::class.java)

    @Test
    @DisplayName("Should bind routes and their rules from gateway.routes")
    fun `should bind routes and rules`() {
        runner.withPropertyValues(
            "gateway.routes[0].id=asset-service",
            "gateway.routes[0].prefix=/assets",
            "gateway.routes[0].uri=http://asset:8080",
            "gateway.routes[0].rules[0].method=PUT",
            "gateway.routes[0].rules[0].path=/devices/{deviceId}",
            "gateway.routes[0].rules[0].role=ADMIN",
            "gateway.routes[1].id=identity-service",
            "gateway.routes[1].prefix=/identity",
            "gateway.routes[1].uri=http://identity:8080"
        ).run { context ->
            val props = context.getBean(GatewayRoutesProperties::class.java)

            assertEquals(2, props.routes.size)
            val asset = props.routes[0]
            assertEquals("asset-service", asset.id)
            assertEquals("/assets", asset.prefix)
            assertEquals("http://asset:8080", asset.uri)
            assertEquals(GatewayRoutesProperties.Route.Rule("PUT", "/devices/{deviceId}", "ADMIN"), asset.rules.single())
            assertTrue(props.routes[1].rules.isEmpty())
        }
    }

    @Test
    @DisplayName("Should default to no routes when nothing is configured")
    fun `should default to empty routes`() {
        runner.run { context ->
            assertTrue(context.getBean(GatewayRoutesProperties::class.java).routes.isEmpty())
        }
    }

}
