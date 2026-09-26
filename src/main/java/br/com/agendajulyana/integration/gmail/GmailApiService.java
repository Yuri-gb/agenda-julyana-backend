package br.com.agendajulyana.integration.gmail;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

@Service
public class GmailApiService {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String SEND_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

    private final GmailApiProperties properties;
    private final RestClient restClient;

    public GmailApiService(GmailApiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public void enviar(String destinatario, String nome, String assunto, String html, String texto) {
        try {
            var accessToken = obterAccessToken();
            var rawMessage = criarMensagem(destinatario, nome, assunto, html, texto);

            restClient.post()
                    .uri(SEND_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("raw", rawMessage))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível enviar o e-mail pela Gmail API.", e);
        }
    }

    private String obterAccessToken() {
        var response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("client_id=" + encode(properties.clientId())
                        + "&client_secret=" + encode(properties.clientSecret())
                        + "&refresh_token=" + encode(properties.refreshToken())
                        + "&grant_type=refresh_token")
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.access_token() == null || response.access_token().isBlank()) {
            throw new IllegalStateException("Google não retornou um access token.");
        }

        return response.access_token();
    }

    private String criarMensagem(String destinatario, String nome, String assunto, String html, String texto)
            throws Exception {
        var session = Session.getInstance(new Properties());
        var message = new MimeMessage(session);
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(destinatario, nome));
        message.setFrom(new InternetAddress(properties.from()));
        message.setSubject(assunto, StandardCharsets.UTF_8.name());

        var multipart = new jakarta.mail.Multipart() {
            @Override
            public jakarta.mail.BodyPart getBodyPart(int index) {
                return null;
            }

            @Override
            public int getCount() {
                return 0;
            }
        };
        // A montagem abaixo usa MimeMultipart para manter texto alternativo e HTML.
        var alternative = new jakarta.mail.internet.MimeMultipart("alternative");
        var plain = new jakarta.mail.internet.MimeBodyPart();
        plain.setText(texto, StandardCharsets.UTF_8.name());
        var rich = new jakarta.mail.internet.MimeBodyPart();
        rich.setContent(html, "text/html; charset=UTF-8");
        alternative.addBodyPart(plain);
        alternative.addBodyPart(rich);
        message.setContent(alternative);

        var output = new ByteArrayOutputStream();
        message.writeTo(output);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(output.toByteArray());
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record TokenResponse(String access_token, String expires_in, String token_type) {
    }
}
