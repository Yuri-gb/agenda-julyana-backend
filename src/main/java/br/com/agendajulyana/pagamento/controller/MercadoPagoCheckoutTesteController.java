package br.com.agendajulyana.pagamento.controller;

import br.com.agendajulyana.pagamento.integration.MercadoPagoClient;
import br.com.agendajulyana.pagamento.integration.MercadoPagoOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/api/dev/pagamentos")
@Tag(name = "Testes", description = "Endpoints de diagnóstico e testes controlados.")
@ConditionalOnProperty(
        name = "app.mercadopago.test-endpoint.enabled",
        havingValue = "true"
)
public class MercadoPagoCheckoutTesteController {
    private static final Logger log = LoggerFactory.getLogger(MercadoPagoCheckoutTesteController.class);

    private final MercadoPagoClient mercadoPago;

    public MercadoPagoCheckoutTesteController(MercadoPagoClient mercadoPago) {
        this.mercadoPago = mercadoPago;
    }

    @PostMapping("/testar-checkout")
    @Operation(
            summary = "Diagnosticar geração de checkout do Mercado Pago",
            description = "Cria uma order sintética de baixo valor, sem agendamento ou gravação de pagamento local.",
            tags = {"Testes"}
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> testarCheckout() {
        try {
            MercadoPagoOrderResponse order = mercadoPago.criarOrderTeste();
            if (order == null || order.id() == null || order.checkout_url() == null
                    || order.checkout_url().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                        new CheckoutTesteResponse(false, null, null, null,
                                "O Mercado Pago respondeu sem ID ou URL de checkout.")
                );
            }

            return ResponseEntity.ok(new CheckoutTesteResponse(
                    true, order.id(), order.checkout_url(), order.status(),
                    "Checkout de teste criado. Nenhum pagamento foi confirmado pelo backend."
            ));
        } catch (RestClientResponseException ex) {
            log.warn("Falha ao criar checkout sintético no Mercado Pago: HTTP {}", ex.getStatusCode().value());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    new CheckoutTesteResponse(false, null, null, null,
                            "O Mercado Pago recusou a criação do checkout (HTTP "
                                    + ex.getStatusCode().value() + "). Verifique o token e o payload de teste.")
            );
        } catch (RuntimeException ex) {
            log.error("Falha inesperada no diagnóstico de checkout do Mercado Pago.", ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    new CheckoutTesteResponse(false, null, null, null,
                            "Não foi possível criar o checkout. Consulte os logs do backend.")
            );
        }
    }

    public record CheckoutTesteResponse(
            boolean sucesso,
            String pedidoId,
            String checkoutUrl,
            String status,
            String mensagem
    ) {}
}
