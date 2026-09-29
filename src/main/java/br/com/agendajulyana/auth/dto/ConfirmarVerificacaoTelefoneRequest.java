package br.com.agendajulyana.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmarVerificacaoTelefoneRequest(
        @NotBlank(message = "Código é obrigatório.")
        @Pattern(regexp = "^\\d{6}$", message = "Código deve conter 6 dígitos.")
        String codigo
) {}
