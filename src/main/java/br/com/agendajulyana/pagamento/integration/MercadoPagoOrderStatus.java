package br.com.agendajulyana.pagamento.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record MercadoPagoOrderStatus(
        String id,
        String status,
        String statusDetail,
        @JsonProperty("external_reference") String externalReference,
        @JsonProperty("total_amount") BigDecimal totalAmount
) {}
