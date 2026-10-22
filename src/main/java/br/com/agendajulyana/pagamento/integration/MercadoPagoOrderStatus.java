package br.com.agendajulyana.pagamento.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record MercadoPagoOrderStatus(
        String id,
        String status,
        String statusDetail,
        @JsonProperty("external_reference") String externalReference,
        @JsonProperty("total_amount") BigDecimal totalAmount,
        Transactions transactions
) {
    public record Transactions(List<Payment> payments) {}
    public record Payment(String id, @JsonProperty("reference_id") String referenceId) {}
}