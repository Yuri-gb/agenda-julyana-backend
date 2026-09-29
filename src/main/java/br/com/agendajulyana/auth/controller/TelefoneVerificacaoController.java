package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.dto.ConfirmarVerificacaoTelefoneRequest;
import br.com.agendajulyana.auth.dto.MeResponse;
import br.com.agendajulyana.auth.dto.SolicitarVerificacaoTelefoneRequest;
import br.com.agendajulyana.auth.service.VerificacaoTelefoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/me/telefone")
@Tag(name = "Autenticação", description = "Verificação do telefone por WhatsApp.")
public class TelefoneVerificacaoController {
    private final VerificacaoTelefoneService service;

    public TelefoneVerificacaoController(VerificacaoTelefoneService service) {
        this.service = service;
    }

    @PostMapping("/verificacao")
    @Operation(summary = "Enviar código de verificação por WhatsApp")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> solicitar(
            Authentication authentication,
            @Valid @RequestBody SolicitarVerificacaoTelefoneRequest request
    ) {
        service.solicitar(authentication.getName(), request.telefone());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/verificacao/confirmar")
    @Operation(summary = "Confirmar código de verificação do telefone")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MeResponse> confirmar(
            Authentication authentication,
            @Valid @RequestBody ConfirmarVerificacaoTelefoneRequest request
    ) {
        return ResponseEntity.ok(service.confirmar(authentication.getName(), request.codigo()));
    }
}