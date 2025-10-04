package com.evolforge.core.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rielHomeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RielHome API")
                        .version("v1")
                        .description("API documentation for the RielHome platform.")
                        .contact(new Contact().name("RielHome Team").email("support@rielhome.com"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(new Server().url("/").description("Default server")))
                .components(new Components().addSecuritySchemes(
                        "BearerAuth",
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
