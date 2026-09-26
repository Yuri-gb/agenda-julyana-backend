package br.com.agendajulyana.pagamento.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercadopago")
public record MercadoPagoProperties(
        String accessToken,
        String baseUrl,
        String testPayerEmail
) {
}
