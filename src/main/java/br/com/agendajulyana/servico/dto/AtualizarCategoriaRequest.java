package br.com.agendajulyana.servico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarCategoriaRequest(
    @NotBlank @Size(max = 100) String nome,
    @Size(max = 5000) String descricao
) {}
