package br.com.agendajulyana.pagamento.integration;

import br.com.agendajulyana.agendamento.domain.Agendamento;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MercadoPagoClient {

    private static final BigDecimal VALOR_CHECKOUT_TESTE = new BigDecimal("10.00");
    private static final String EMAIL_PAGADOR_TESTE_PADRAO = "test@testuser.com";

    private final RestClient client;
    private final MercadoPagoProperties properties;

    public MercadoPagoClient(RestClient mercadoPagoRestClient, MercadoPagoProperties properties) {
        this.client = mercadoPagoRestClient;
        this.properties = properties;
    }

    public MercadoPagoOrderResponse criarOrder(Agendamento agendamento, BigDecimal valor, UUID idempotencyKey) {
        var usuario = agendamento.getCliente().getUsuario();
        var payerEmail = properties.testPayerEmail() != null && !properties.testPayerEmail().isBlank()
                ? properties.testPayerEmail()
                : usuario.getEmail();

        var request = new MercadoPagoOrderRequest(
                "online",
                valor.toPlainString(),
                agendamento.getId().toString(),
                "manual",
                "automatic_async",
                new Payer(payerEmail),
                new Item(
                        "Agendamento - " + agendamento.getServico().getNome(),
                        valor.toPlainString(),
                        1,
                        valor.toPlainString()
                )
        );

        return enviarOrder(request, idempotencyKey);
    }

    /**
     * Cria uma order sintética para diagnosticar a geração do checkout.
     * Não cria agendamento nem grava/atualiza pagamentos no banco local.
     */
    public MercadoPagoOrderResponse criarOrderTeste() {
        // Endpoint de diagnóstico nunca deve enviar pedidos usando credenciais de produção.
        if (properties.accessToken() == null || !properties.accessToken().startsWith("TEST-")) {
            throw new IllegalStateException(
                    "O diagnóstico de checkout exige um access token de teste do Mercado Pago (prefixo TEST-)."
            );
        }

        var emailTeste = properties.testPayerEmail() != null && !properties.testPayerEmail().isBlank()
                ? properties.testPayerEmail()
                : EMAIL_PAGADOR_TESTE_PADRAO;
        var valor = VALOR_CHECKOUT_TESTE;
        var referencia = "checkout-test-" + UUID.randomUUID();
        var request = new MercadoPagoOrderRequest(
                "online",
                valor.toPlainString(),
                referencia,
                "manual",
                "automatic_async",
                new Payer(emailTeste),
                new Item("Teste de geração de checkout", valor.toPlainString(), 1, valor.toPlainString())
        );

        return enviarOrder(request, UUID.randomUUID());
    }

    private MercadoPagoOrderResponse enviarOrder(MercadoPagoOrderRequest request, UUID idempotencyKey) {
        return client.post()
                .uri("/v1/orders")
                .header("X-Idempotency-Key", idempotencyKey.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MercadoPagoOrderResponse.class);
    }

    public MercadoPagoOrderStatus consultarOrder(String orderId) {
        return client.get()
                .uri("/v1/orders/{id}", orderId)
                .retrieve()
                .body(MercadoPagoOrderStatus.class);
    }

    record MercadoPagoOrderRequest(
            String type,
            String total_amount,
            String external_reference,
            String processing_mode,
            String capture_mode,
            Payer payer,
            Item[] items
    ) {
        MercadoPagoOrderRequest(
                String type,
                String totalAmount,
                String externalReference,
                String processingMode,
                String captureMode,
                Payer payer,
                Item item
        ) {
            this(type, totalAmount, externalReference, processingMode, captureMode, payer, new Item[]{item});
        }
    }

    record Payer(String email) {}

    public MercadoPagoRefundResponse reembolsarOrder(String orderId, String transactionId, BigDecimal amount, boolean total, UUID idempotencyKey) {
        var request = client.post()
                .uri("/v1/orders/{id}/refund", orderId)
                .header("X-Idempotency-Key", idempotencyKey.toString())
                .contentType(MediaType.APPLICATION_JSON);

        if (!total) {
            request.body(new MercadoPagoRefundRequest(
                    new MercadoPagoRefundTransaction[]{new MercadoPagoRefundTransaction(transactionId, amount.toPlainString())}
            ));
        }

        return request.retrieve().body(MercadoPagoRefundResponse.class);
    }

    record Item(
            String title,
            String unit_price,
            int quantity,
            String total_amount
    ) {}

    record MercadoPagoRefundRequest(MercadoPagoRefundTransaction[] transactions) {}
    record MercadoPagoRefundTransaction(String id, String amount) {}

    public record MercadoPagoRefundResponse(
            String id,
            String status,
            @JsonProperty("status_detail") String statusDetail,
            Transactions transactions
    ) {
        public record Transactions(java.util.List<Refund> refunds) {}
        public record Refund(String id, @JsonProperty("transaction_id") String transactionId, BigDecimal amount, String status) {}
    }
}
