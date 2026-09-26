package br.com.agendajulyana.integration.gmail;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GmailOAuthService {

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String SCOPE = "https://www.googleapis.com/auth/gmail.send";

    private final GmailApiProperties properties;
    private final RestClient restClient;
    private final Map<String, Instant> states = new ConcurrentHashMap<>();

    public GmailOAuthService(GmailApiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public String criarUrlAutorizacao() {
        var state = UUID.randomUUID().toString();
        states.put(state, Instant.now());

        return AUTH_URL
                + "?client_id=" + encode(properties.clientId())
                + "&redirect_uri=" + encode(properties.redirectUri())
                + "&response_type=code"
                + "&scope=" + encode(SCOPE)
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + encode(state);
    }

    public String trocarCodigoPorRefreshToken(String code, String state) {
        var criadoEm = states.remove(state);
        if (criadoEm == null || criadoEm.plusSeconds(600).isBefore(Instant.now())) {
            throw new IllegalArgumentException("Estado OAuth inválido ou expirado.");
        }

        var form = new LinkedMultiValueMap<String, String>();
        form.add("code", code);
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("redirect_uri", properties.redirectUri());
        form.add("grant_type", "authorization_code");

        var response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.refresh_token() == null || response.refresh_token().isBlank()) {
            throw new IllegalStateException(
                    "Google não retornou refresh_token. Verifique access_type=offline e a autorização da conta remetente."
            );
        }

        return response.refresh_token();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record TokenResponse(
            String access_token,
            String expires_in,
            String refresh_token,
            String token_type
    ) {
    }
}
