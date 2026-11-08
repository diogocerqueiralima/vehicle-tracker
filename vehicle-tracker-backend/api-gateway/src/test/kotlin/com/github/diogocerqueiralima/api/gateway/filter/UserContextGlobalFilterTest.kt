package com.github.diogocerqueiralima.api.gateway.filter

import com.github.diogocerqueiralima.api.common.headers.ReservedHeaders
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.core.Ordered
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull

@ExtendWith(MockitoExtension::class)
class UserContextGlobalFilterTest {

    @Mock
    private lateinit var chain: GatewayFilterChain

    private val filter = UserContextGlobalFilter()

    private fun exchange(vararg headers: Pair<String, String>): MockServerWebExchange {
        val builder = MockServerHttpRequest.get("/assets/devices")
        headers.forEach { builder.header(it.first, it.second) }
        return MockServerWebExchange.from(builder)
    }

    private fun jwtAuth(vararg roles: String, username: String? = "john"): JwtAuthenticationToken {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("user-1")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .apply { if (username != null) claim("preferred_username", username) }
            .build()
        return JwtAuthenticationToken(jwt, roles.map { SimpleGrantedAuthority(it) })
    }

    private fun run(exchange: ServerWebExchange, auth: org.springframework.security.core.Authentication?): ServerHttpRequest {
        val captor = ArgumentCaptor.forClass(ServerWebExchange::class.java)
        // NOTE: captor.value is the first captured call
        org.mockito.Mockito.`when`(chain.filter(captor.capture())).thenReturn(Mono.empty())

        var mono = filter.filter(exchange, chain)
        if (auth != null) {
            mono = mono.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(SecurityContextImpl(auth))))
        }

        StepVerifier.create(mono).verifyComplete()
        return captor.allValues.first().request
    }

    @Test
    @DisplayName("Should invoke the downstream chain exactly once when authenticated")
    fun `should call chain once when authenticated`() {
        org.mockito.Mockito.`when`(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty())

        StepVerifier.create(
            filter.filter(exchange(), chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(SecurityContextImpl(jwtAuth("ROLE_USER")))))
        ).verifyComplete()

        org.mockito.Mockito.verify(chain, org.mockito.Mockito.times(1)).filter(org.mockito.ArgumentMatchers.any())
    }

    @Test
    @DisplayName("Should inject user id, username and roles from the JWT when authenticated")
    fun `should inject user context headers from jwt`() {
        val request = run(exchange(), jwtAuth("ROLE_ADMIN", "ROLE_USER"))

        assertEquals("user-1", request.headers.getFirst(ReservedHeaders.USER_ID))
        assertEquals("john", request.headers.getFirst(ReservedHeaders.USER_USERNAME))
        assertEquals("ADMIN,USER", request.headers.getFirst(ReservedHeaders.USER_ROLES))
    }

    @Test
    @DisplayName("Should overwrite spoofed client headers with values from the JWT")
    fun `should replace spoofed headers with jwt values`() {
        val request = run(
            exchange(
                ReservedHeaders.USER_ID to "attacker",
                ReservedHeaders.USER_ROLES to "ADMIN",
                ReservedHeaders.USER_USERNAME to "evil"
            ),
            jwtAuth("ROLE_USER")
        )

        assertEquals(listOf("user-1"), request.headers[ReservedHeaders.USER_ID])
        assertEquals(listOf("USER"), request.headers[ReservedHeaders.USER_ROLES])
        assertEquals(listOf("john"), request.headers[ReservedHeaders.USER_USERNAME])
    }

    @Test
    @DisplayName("Should send an empty username when the JWT has no preferred_username claim")
    fun `should use empty username when claim missing`() {
        val request = run(exchange(), jwtAuth("ROLE_USER", username = null))

        assertEquals("", request.headers.getFirst(ReservedHeaders.USER_USERNAME))
    }

    @Test
    @DisplayName("Should send empty roles when the JWT has no authorities")
    fun `should send empty roles when no authorities`() {
        val request = run(exchange(), jwtAuth())

        assertEquals("", request.headers.getFirst(ReservedHeaders.USER_ROLES))
    }

    @Test
    @DisplayName("Should strip spoofed headers and not inject any when there is no security context")
    fun `should strip headers when unauthenticated`() {
        val request = run(
            exchange(
                ReservedHeaders.USER_ID to "attacker",
                ReservedHeaders.USER_ROLES to "ADMIN",
                ReservedHeaders.USER_USERNAME to "evil"
            ),
            null
        )

        assertNull(request.headers.getFirst(ReservedHeaders.USER_ID))
        assertNull(request.headers.getFirst(ReservedHeaders.USER_ROLES))
        assertNull(request.headers.getFirst(ReservedHeaders.USER_USERNAME))
    }

    @Test
    @DisplayName("Should strip spoofed headers and not inject any when the authentication is not a JWT")
    fun `should strip headers when authentication is not jwt`() {
        val auth = UsernamePasswordAuthenticationToken("u", "p", listOf(SimpleGrantedAuthority("ROLE_ADMIN")))

        val request = run(exchange(ReservedHeaders.USER_ID to "attacker"), auth)

        assertNull(request.headers.getFirst(ReservedHeaders.USER_ID))
        assertNull(request.headers.getFirst(ReservedHeaders.USER_ROLES))
    }

    @Test
    @DisplayName("Should run last in the filter order")
    fun `should have lowest precedence`() {
        assertEquals(Ordered.LOWEST_PRECEDENCE, filter.order)
    }

}
