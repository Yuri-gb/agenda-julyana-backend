package br.com.agendajulyana.disponibilidade.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BloqueioResponse(UUID id, OffsetDateTime inicio, OffsetDateTime fim, String motivo, UUID criadoPorId, OffsetDateTime criadoEm) {}