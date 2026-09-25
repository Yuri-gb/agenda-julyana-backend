package br.com.agendajulyana.servico.dto;

import jakarta.validation.constraints.NotNull;

public record AlterarStatusCategoriaRequest(@NotNull Boolean ativo) {}
