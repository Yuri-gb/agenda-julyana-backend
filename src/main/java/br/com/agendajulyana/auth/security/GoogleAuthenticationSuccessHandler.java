package br.com.agendajulyana.auth.security;

import br.com.agendajulyana.auth.service.GoogleAuthenticationService;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;

@Component
public class GoogleAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final GoogleAuthenticationService service;
    private final String callback;

    public GoogleAuthenticationSuccessHandler(
        GoogleAuthenticationService service,
        @Value("${app.security.google.frontend-callback}") String callback) {
        this.service = service; this.callback = callback;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            String token = service.authenticate((OAuth2User) authentication.getPrincipal());
            response.sendRedirect(UriComponentsBuilder.fromUriString(callback)
                .fragment("token=" + token).build().toUriString());
        } catch (IllegalStateException ex) {
            response.sendRedirect(UriComponentsBuilder.fromUriString(callback)
                .queryParam("error", "google_authentication_failed").build().toUriString());
        }
    }
}
