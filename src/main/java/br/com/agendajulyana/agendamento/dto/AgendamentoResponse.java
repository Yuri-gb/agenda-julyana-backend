package br.com.agendajulyana.agendamento.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AgendamentoResponse(UUID id,UUID servicoId,OffsetDateTime inicio,OffsetDateTime fim,int duracaoMinutos,BigDecimal valorServico,BigDecimal valorEntrada,String status,UUID reservaId,OffsetDateTime reservaExpiraEm){}
