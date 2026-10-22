package br.com.agendajulyana.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI agendaJulyanaOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Agenda Julyana API").version("v1").description("API do sistema de agendamento da Agenda Julyana."))
            .tags(List.of(
                new Tag().name("Autenticação").description("Identidade, login, cadastro e recuperação de acesso."),
                new Tag().name("Cliente — Agendamentos").description("Operações de agendamento realizadas pelo cliente."),
                new Tag().name("Cliente — Pagamentos").description("Operações de pagamento iniciadas pelo cliente."),
                new Tag().name("Cliente — Serviços").description("Consulta e utilização dos serviços pelo cliente."),
                new Tag().name("Admin — Usuários").description("Operações administrativas relacionadas a usuários."),
                new Tag().name("Admin — Agendamentos").description("Operações administrativas sobre a agenda e os atendimentos."),
                new Tag().name("Admin — Disponibilidade").description("Configuração de disponibilidade, bloqueios e indisponibilidades."),
                new Tag().name("Admin — Serviços").description("Gerenciamento administrativo de serviços."),
                new Tag().name("Admin — Categorias").description("Gerenciamento administrativo de categorias de serviços."),
                new Tag().name("Integração — Mercado Pago").description("Endpoints utilizados pelo Mercado Pago para atualização do pagamento."),
                new Tag().name("Integração — Google Gmail").description("Autorização da conta Gmail usada para envio de e-mails."),
                new Tag().name("Testes — E-mail").description("Endpoint técnico de teste de envio de e-mails, quando habilitado.")
            ))
            .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
            .path("/oauth2/authorization/google", new PathItem().get(new Operation().summary("Entrar com Google").description("Inicia o fluxo OAuth2 com o Google.").operationId("loginWithGoogle").tags(List.of("Autenticação"))));
    }
}
