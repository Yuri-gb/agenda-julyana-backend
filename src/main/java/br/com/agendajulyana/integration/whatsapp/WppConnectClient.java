package br.com.agendajulyana.integration.whatsapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.whatsapp.provider", havingValue = "wppconnect", matchIfMissing = true)
public class WppConnectClient implements WhatsAppMessageSender {

    private final RestClient client;
    private final String token;
    private final String session;

    @Autowired
    public WppConnectClient(
            @Value("${app.whatsapp.wppconnect.base-url:http://localhost:21465}") String baseUrl,
            @Value("${app.whatsapp.wppconnect.token:}") String token,
            @Value("${app.whatsapp.wppconnect.session:agenda-julyana}") String session
    ) {
        this(
                RestClient.builder().baseUrl(normalizeBaseUrl(baseUrl)),
                token,
                session
        );
    }

    WppConnectClient(RestClient.Builder clientBuilder, String token, String session) {
        this.client = clientBuilder.build();
        this.token = token;
        this.session = session;
    }

    @Override
    public void sendVerificationCode(String telefone, String codigo) {
        ensureConfigured();

        client.post()
                .uri("/api/{session}/send-message", session)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "phone", telefone,
                        "message", "Seu código de verificação é: " + codigo
                ))
                .retrieve()
                .toBodilessEntity();
    }

    public String startSession() {
        ensureConfigured();

        return client.post()
                .uri("/api/{session}/start-session", session)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(String.class);
    }

    public String statusSession() {
        ensureConfigured();

        return client.get()
                .uri("/api/{session}/status-session", session)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(String.class);
    }

    private void ensureConfigured() {
        if (token.isBlank() || session.isBlank()) {
            throw new IllegalStateException("Integração WPPConnect não configurada.");
        }
    }

    private static String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}