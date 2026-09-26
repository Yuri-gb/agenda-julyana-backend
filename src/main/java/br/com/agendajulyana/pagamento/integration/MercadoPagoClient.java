package br.com.agendajulyana.pagamento.integration;

import br.com.agendajulyana.agendamento.domain.Agendamento;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MercadoPagoClient {

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

    record Item(
            String title,
            String unit_price,
            int quantity,
            String total_amount
    ) {}
}
