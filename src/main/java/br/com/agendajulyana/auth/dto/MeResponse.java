package br.com.agendajulyana.auth.dto;

import java.util.Set;
import java.util.UUID;

public record MeResponse(
        UUID usuarioId,
        String nome,
        String email,
        String telefone,
        String status,
        Set<String> papeis,
        boolean telefoneVerificado
) {}