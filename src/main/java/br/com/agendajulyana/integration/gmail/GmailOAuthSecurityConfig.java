package br.com.agendajulyana.integration.gmail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(name = "app.gmail.authorization.enabled", havingValue = "true")
public class GmailOAuthSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain gmailOAuthSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/integrations/google/gmail/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
