package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.dto.*;
import br.com.agendajulyana.auth.service.PasswordRecoveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class PasswordRecoveryController {
    private final PasswordRecoveryService service;

    public PasswordRecoveryController(PasswordRecoveryService service) {
        this.service = service;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> solicitar(
        @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(service.solicitar(request.email()));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<VerifyResetCodeResponse> verificarCodigo(
        @Valid @RequestBody VerifyResetCodeRequest request
    ) {
        return ResponseEntity.ok(service.verificarCodigo(request.email(), request.codigo()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> redefinirSenha(
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(service.redefinirSenha(request));
    }
}
