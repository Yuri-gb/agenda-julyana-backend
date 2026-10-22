package br.com.agendajulyana.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SolicitarVerificacaoTelefoneRequest(
        @NotBlank(message = "Telefone é obrigatório.")
        @Pattern(regexp = "^\\d{10,15}$", message = "Telefone deve conter apenas dígitos.")
        String telefone
) {}
