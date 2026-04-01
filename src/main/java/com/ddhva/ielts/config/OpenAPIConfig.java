package com.ddhva.ielts.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public GroupedOpenApi publicApi(@Value("${OpenAPI.service.api-docs}") String apiDocs) {
        return GroupedOpenApi
                .builder()
                .group(apiDocs)
                .packagesToScan("com.ddhva.ielts.controller")
                .build();
    }


    @Bean
    public OpenAPI openAPI(
            @Value("${OpenAPI.service.title}") String title,
            @Value("${OpenAPI.service.version}") String version,
            @Value("${OpenAPI.service.server}") String serverUrl) {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .servers(List.of(new Server().url(serverUrl)))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .security(List.of(new SecurityRequirement().addList(securitySchemeName)))
                .info(new Info().title(title)
                        .description("Service IELTS API")
                        .version(version)
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}