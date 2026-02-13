package com.github.diogocerqueiralima.presentation.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecuritySchemes(
        value = {
                @SecurityScheme(
                        name = "bearerAuth",
                        type = SecuritySchemeType.HTTP,
                        scheme = "bearer",
                        bearerFormat = "JWT"
                ),
                @SecurityScheme(
                        name = "certificateAuth",
                        type = SecuritySchemeType.APIKEY,
                        in = SecuritySchemeIn.HEADER,
                        paramName = "X-Client-Cert-CN"
                )
        }
)
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Identity Service API")
                                .version("1.0")
                                .description(
                                        """
                                        This API provides endpoints for managing bootstrap certificates and
                                        regular certificates, including retrieval, signing, and revocation operations.
                                        The API is secured using JWT bearer tokens for bootstrap certificate operations
                                        and client certificate authentication for regular certificate operations.
                                        """
                                )
                )
                .addServersItem(new Server().url("https://mytracker.pt"))
                .addServersItem(new Server().url("http://localhost:8080"));
    }

}
