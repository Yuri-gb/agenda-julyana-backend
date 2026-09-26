package br.com.agendajulyana.pagamento.controller;

import br.com.agendajulyana.pagamento.dto.CheckoutPagamentoResponse;
import br.com.agendajulyana.pagamento.integration.MercadoPagoWebhookValidator;
import br.com.agendajulyana.pagamento.service.PagamentoService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PagamentoController {
    private final PagamentoService pagamentos;
    private final MercadoPagoWebhookValidator webhookValidator;

    public PagamentoController(PagamentoService pagamentos, MercadoPagoWebhookValidator webhookValidator) {
        this.pagamentos = pagamentos;
        this.webhookValidator = webhookValidator;
    }

    @PostMapping("/agendamentos/{id}/pagamento/checkout")
    public ResponseEntity<CheckoutPagamentoResponse> criarCheckout(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        var response = pagamentos.criarCheckout(UUID.fromString(authentication.getName()), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhooks/mercado-pago")
    public ResponseEntity<Void> receberWebhook(
            @RequestParam(name="data.id") String dataId,
            @RequestParam(name="type", required=false) String type,
            @RequestHeader(name="x-signature", required=false) String signature,
            @RequestHeader(name="x-request-id", required=false) String requestId
    ) {
        if (!"order".equalsIgnoreCase(type)
                || !webhookValidator.isValid(signature, requestId, dataId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        pagamentos.processarWebhookOrder(dataId);
        return ResponseEntity.ok().build();
    }
}
