package com.inventrik.digitalestore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${oauth2.issuer:http://localhost:8080}")
    private String issuerUri;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital E-Store API")
                        .version("1.0.0")
                        .description("API for managing digital products and services")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com")
                                .url("yourdomain.com")))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                .components(new Components()
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(issuerUri + "/oauth2/authorize")
                                                .tokenUrl(issuerUri + "/oauth2/token")
                                                .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                                        .addString("read", "read access")
                                                        .addString("write", "write access"))))));
    }
}