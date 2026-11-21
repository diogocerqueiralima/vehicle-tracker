package com.github.diogocerqueiralima.api.gateway.config

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "KEYCLOAK_ISSUER_URI=https://keycloak.example/realms/tracker",
        "ASSET_SERVICE_URI=http://localhost:18081",
        "IDENTITY_SERVICE_URI=http://localhost:18082"
    ]
)
class GatewayHealthTest {

    @LocalServerPort
    private var port: Int = 0

    private val client = HttpClient.newHttpClient()

    @Test
    fun `health probe is accessible without a token`() {
        val response = send("GET", "/actuator/health")

        assertEquals(200, response.statusCode())
        assertEquals("UP", JsonPath.read<String>(response.body(), "$.status"))
    }

    @Test
    fun `health probe does not open other endpoints or methods`() {
        assertEquals(401, send("GET", "/actuator/env").statusCode())
        assertEquals(401, send("POST", "/actuator/health").statusCode())
        assertEquals(401, send("GET", "/assets/devices").statusCode())
        assertEquals(401, send("GET", "/identity/certificates").statusCode())
    }

    private fun send(method: String, path: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path"))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
}
