package com.github.diogocerqueiralima.api.gateway.config

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class SecurityConfigTest {

    private val converter = SecurityConfig.KeycloakRealmRoleConverter()

    private fun jwt(claims: Map<String, Any>): Jwt =
        Jwt.withTokenValue("t").header("alg", "none").subject("user-1").claims { it.putAll(claims) }.build()

    @Test
    @DisplayName("Should map tracker client roles to upper-case ROLE_ authorities")
    fun `should map tracker roles`() {
        val result = converter.convert(
            jwt(mapOf("resource_access" to mapOf("tracker" to mapOf("roles" to listOf("admin", "User")))))
        )

        assertEquals(listOf("ROLE_ADMIN", "ROLE_USER"), result.map { it.authority })
    }

    @Test
    @DisplayName("Should ignore roles of other clients")
    fun `should ignore other clients`() {
        val result = converter.convert(
            jwt(mapOf("resource_access" to mapOf("other" to mapOf("roles" to listOf("admin")))))
        )

        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("Should return no authorities when resource_access claim is missing")
    fun `should return empty without resource_access`() {
        assertTrue(converter.convert(jwt(mapOf("foo" to "bar"))).isEmpty())
    }

    @Test
    @DisplayName("Should skip non-string roles")
    fun `should skip non string roles`() {
        val result = converter.convert(
            jwt(mapOf("resource_access" to mapOf("tracker" to mapOf("roles" to listOf("admin", 1, null)))))
        )

        assertEquals(listOf("ROLE_ADMIN"), result.map { it.authority })
    }

    @Test
    @DisplayName("Should return no authorities when roles is not a collection")
    fun `should handle malformed roles`() {
        val result = converter.convert(
            jwt(mapOf("resource_access" to mapOf("tracker" to mapOf("roles" to "admin"))))
        )

        assertTrue(result.isEmpty())
    }

}
