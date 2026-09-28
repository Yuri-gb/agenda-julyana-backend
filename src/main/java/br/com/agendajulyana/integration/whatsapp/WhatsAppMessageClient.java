package br.com.agendajulyana.integration.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class WhatsAppMessageClient {
    private final RestClient client;
    private final String accessToken;
    private final String phoneNumberId;
    private final String apiVersion;

    public WhatsAppMessageClient(
            @Value("${WHATSAPP_ACCESS_TOKEN:}") String accessToken,
            @Value("${WHATSAPP_PHONE_NUMBER_ID:1282297904975456}") String phoneNumberId,
            @Value("${WHATSAPP_API_VERSION:v26.0}") String apiVersion
    ) {
        this.accessToken = accessToken;
        this.phoneNumberId = phoneNumberId;
        this.apiVersion = apiVersion;
        this.client = RestClient.builder()
                .baseUrl("https://graph.facebook.com/" + apiVersion)
                .build();
    }

    public void sendTemplate(String telefone, String templateName, String languageCode, String... bodyParameters) {
        if (accessToken.isBlank() || phoneNumberId.isBlank()) {
            throw new IllegalStateException("Integração WhatsApp não configurada.");
        }

        var parameters = List.of(bodyParameters).stream()
                .map(value -> Map.of("type", "text", "text", value))
                .toList();

        var body = Map.of(
                "messaging_product", "whatsapp",
                "to", telefone,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", languageCode),
                        "components", List.of(Map.of(
                                "type", "body",
                                "parameters", parameters
                        ))
                )
        );

        client.post()
                .uri("/" + phoneNumberId + "/messages")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
