package com.github.diogocerqueiralima.asset.service.presentation.http.config;

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
                )
        }
)
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Asset Service API")
                                .version("1.0")
                                .description(
                                        """
                                        This API provides endpoints for managing assets, including devices and vehicles.
                                        This API is secured using JWT bearer tokens for authentication and authorization.
                                        """
                                )
                )
                .addServersItem(new Server().url("https://tracker.homelab"))
                .addServersItem(new Server().url("http://localhost:8080"));
    }

}
