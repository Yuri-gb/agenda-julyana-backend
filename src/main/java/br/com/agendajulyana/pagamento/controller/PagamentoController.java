package br.com.agendajulyana.pagamento.controller;

import br.com.agendajulyana.pagamento.dto.CheckoutPagamentoResponse;
import br.com.agendajulyana.pagamento.service.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")
public class PagamentoController {

    private final PagamentoService pagamentos;

    public PagamentoController(PagamentoService pagamentos) {
        this.pagamentos = pagamentos;
    }

    @PostMapping("/{id}/pagamento/checkout")
    public ResponseEntity<CheckoutPagamentoResponse> criarCheckout(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        var response = pagamentos.criarCheckout(UUID.fromString(authentication.getName()), id);
        return ResponseEntity.ok(response);
    }
}
