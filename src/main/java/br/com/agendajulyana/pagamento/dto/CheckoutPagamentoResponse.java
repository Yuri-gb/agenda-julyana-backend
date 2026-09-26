package br.com.agendajulyana.pagamento.dto;

import br.com.agendajulyana.pagamento.domain.PagamentoModalidade;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutPagamentoResponse(
        UUID pagamentoId,
        UUID agendamentoId,
        PagamentoModalidade modalidade,
        BigDecimal valor,
        String orderId,
        String checkoutUrl
) {
}
