package br.com.agendajulyana.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarAdminRequest(
    @NotBlank @Size(max = 150) String nome,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(max = 30) String telefone,
    @NotBlank @Size(min = 8, max = 72) String senha
) {}
