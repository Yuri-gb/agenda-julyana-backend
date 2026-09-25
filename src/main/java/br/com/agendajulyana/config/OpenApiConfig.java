package br.com.agendajulyana.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI agendaJulyanaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Agenda Julyana API")
                .version("v1")
                .description("API do sistema de agendamento da Agenda Julyana."))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .path("/oauth2/authorization/google", new PathItem()
                .get(new Operation()
                    .summary("Entrar com Google")
                    .description("Inicia o fluxo OAuth2 com o Google. No navegador, acesse também: http://localhost:8080/oauth2/authorization/google")
                    .operationId("loginWithGoogle")));
    }
}
