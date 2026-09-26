package br.com.agendajulyana.pagamento.dto;

import br.com.agendajulyana.pagamento.domain.Pagamento;
import br.com.agendajulyana.pagamento.domain.PagamentoModalidade;

import java.math.BigDecimal;
import java.util.UUID;

public record PagamentoResponse(
        UUID id,
        UUID agendamentoId,
        PagamentoModalidade modalidade,
        BigDecimal valor,
        String status,
        String provedor
) {
    public static PagamentoResponse from(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getAgendamento().getId(),
                pagamento.getModalidade(),
                pagamento.getValor(),
                pagamento.getStatus().name(),
                pagamento.getProvedor()
        );
    }
}
