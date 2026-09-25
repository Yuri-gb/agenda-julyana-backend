package br.com.agendajulyana.disponibilidade.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public record IndisponibilidadeServicoRequest(
    @NotNull UUID servicoId,
    @NotNull OffsetDateTime inicio,
    @NotNull OffsetDateTime fim,
    @Size(max=1000) String motivo
) {}