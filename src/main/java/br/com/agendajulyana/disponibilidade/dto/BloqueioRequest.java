package br.com.agendajulyana.disponibilidade.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record BloqueioRequest(
    @NotNull OffsetDateTime inicio,
    @NotNull OffsetDateTime fim,
    @Size(max=1000) String motivo
) {}