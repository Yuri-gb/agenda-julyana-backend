package br.com.agendajulyana.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(
        @Value("${app.security.jwt.secret}") String secretBase64,
        @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64));
        this.expirationMillis = expirationMinutes * 60_000L;
    }

    public String generateToken(String subject) {
        var now = new Date();
        var expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }
}
