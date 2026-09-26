package br.com.agendajulyana.integration.gmail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gmail")
public record GmailApiProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String refreshToken,
        String from
) {
    public boolean configured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank()
                && redirectUri != null && !redirectUri.isBlank()
                && refreshToken != null && !refreshToken.isBlank();
    }
}
