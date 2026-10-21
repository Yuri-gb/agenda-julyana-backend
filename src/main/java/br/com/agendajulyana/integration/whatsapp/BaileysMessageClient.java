package br.com.agendajulyana.integration.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.whatsapp.provider", havingValue = "baileys")
public class BaileysMessageClient implements WhatsAppMessageSender {

    private final RestClient client;
    private final String apiToken;

    public BaileysMessageClient(
            @Value("${app.whatsapp.baileys.base-url:http://localhost:3000}") String baseUrl,
            @Value("${app.whatsapp.baileys.api-token:}") String apiToken
    ) {
        this(RestClient.builder().baseUrl(normalizeBaseUrl(baseUrl)), apiToken);
    }

    BaileysMessageClient(RestClient.Builder clientBuilder, String apiToken) {
        this.client = clientBuilder.build();
        this.apiToken = apiToken;
    }

    @Override
    public void sendVerificationCode(String telefone, String codigo) {
        if (apiToken.isBlank()) {
            throw new IllegalStateException("Integração WhatsApp Baileys não configurada: BAILEYS_API_TOKEN ausente.");
        }

        client.post()
                .uri("/v1/messages")
                .header("Authorization", "Bearer " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "phone", telefone,
                        "message", "Seu código de verificação é: " + codigo
                ))
                .retrieve()
                .toBodilessEntity();
    }

    private static String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
