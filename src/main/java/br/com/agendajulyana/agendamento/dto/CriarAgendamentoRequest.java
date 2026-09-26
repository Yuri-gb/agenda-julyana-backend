package br.com.agendajulyana.agendamento.dto;

import br.com.agendajulyana.pagamento.domain.PagamentoModalidade;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CriarAgendamentoRequest(
        @NotNull UUID servicoId,
        @NotNull OffsetDateTime inicio,
        @NotNull PagamentoModalidade modalidadePagamento
) {}
