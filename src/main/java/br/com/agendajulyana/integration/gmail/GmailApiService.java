package br.com.agendajulyana.integration.gmail;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
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

    public GmailApiService(GmailApiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
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
        var form = new LinkedMultiValueMap<String, String>();
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("refresh_token", properties.refreshToken());
        form.add("grant_type", "refresh_token");

        var response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
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

        var related = new MimeMultipart("related");
        var alternative = new MimeMultipart("alternative");

        var plain = new MimeBodyPart();
        plain.setText(texto, StandardCharsets.UTF_8.name());
        alternative.addBodyPart(plain);

        var rich = new MimeBodyPart();
        rich.setContent(html, "text/html; charset=UTF-8");
        alternative.addBodyPart(rich);

        var content = new MimeBodyPart();
        content.setContent(alternative);
        related.addBodyPart(content);

        var header = new ClassPathResource("email/julyana-email-header.jpg");
        if (!header.exists()) {
            throw new IllegalStateException("Cabeçalho de e-mail não encontrado no classpath.");
        }

        var image = new MimeBodyPart();
        image.setDataHandler(new jakarta.activation.DataHandler(header.getURL()));
        image.setHeader("Content-ID", "<julyana-email-header>");
        image.setDisposition(MimeBodyPart.INLINE);
        related.addBodyPart(image);

        message.setContent(related);

        var output = new ByteArrayOutputStream();
        message.writeTo(output);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(output.toByteArray());
    }

    private record TokenResponse(String access_token, String expires_in, String token_type) {
    }
}
