package br.com.agendajulyana.auth.dto;

import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequest(
    @Size(max = 150) String nome,
    @Size(max = 30) String telefone
) {}
