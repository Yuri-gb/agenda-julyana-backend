package br.com.agendajulyana.pagamento.integration;

public record MercadoPagoOrderResponse(
        String id,
        String checkout_url,
        String status,
        String status_detail
) {
}
