package br.com.agendajulyana.disponibilidade.dto;

import jakarta.validation.constraints.NotNull;

public record DisponibilidadeStatusRequest(@NotNull Boolean ativo) {}