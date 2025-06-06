package com.inventrik.digitalestore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        Scopes scopes = new Scopes();
        scopes.addString("read", "Read access");
        scopes.addString("write", "Write access");
        
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
                                                .authorizationUrl("http://localhost:8080/oauth2/authorize")
                                                .tokenUrl("http://localhost:8080/oauth2/token")
                                                .scopes(scopes))
                                        .clientCredentials(new OAuthFlow()
                                                .tokenUrl("http://localhost:8080/oauth2/token")
                                                .scopes(scopes)))));
    }
}