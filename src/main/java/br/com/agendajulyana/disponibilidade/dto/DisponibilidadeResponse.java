package br.com.agendajulyana.disponibilidade.dto;

import java.time.LocalTime;
import java.util.UUID;

public record DisponibilidadeResponse(UUID id, Integer diaSemana, LocalTime horaInicio, LocalTime horaFim, boolean ativo) {}