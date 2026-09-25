package br.com.agendajulyana.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank String tokenRedefinicao,
    @NotBlank @Size(min = 8, max = 100) String novaSenha,
    @NotBlank @Size(min = 8, max = 100) String confirmacaoSenha
) {}
