package br.com.agendajulyana.disponibilidade.dto;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

public record DisponibilidadeRequest(
    @NotNull @Min(1) @Max(7) Integer diaSemana,
    @NotNull LocalTime horaInicio,
    @NotNull LocalTime horaFim
) {}