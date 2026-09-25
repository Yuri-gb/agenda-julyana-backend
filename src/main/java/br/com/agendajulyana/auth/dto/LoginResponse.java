package br.com.agendajulyana.auth.dto;

import java.util.Set;
import java.util.UUID;

public record LoginResponse(
    UUID usuarioId,
    String nome,
    String email,
    Set<String> papeis,
    String token,
    long expiresInSeconds
) {
}
