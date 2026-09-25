package br.com.agendajulyana.disponibilidade.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IndisponibilidadeServicoResponse(UUID id, UUID servicoId, OffsetDateTime inicio, OffsetDateTime fim, String motivo, OffsetDateTime criadoEm) {}