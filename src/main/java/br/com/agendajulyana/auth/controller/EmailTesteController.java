package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.service.EmailTesteService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/emails")
@ConditionalOnProperty(name = "app.email.test-endpoint.enabled", havingValue = "true")
public class EmailTesteController {
    private final EmailTesteService emailTesteService;

    public EmailTesteController(EmailTesteService emailTesteService) {
        this.emailTesteService = emailTesteService;
    }

    @PostMapping
    public ResponseEntity<EmailTesteResponse> enviar(
            @Parameter(description = "Tipo de e-mail disponível para teste.")
            @RequestParam EmailTesteTipo tipo,
            @Valid @RequestBody EmailTesteRequest request
    ) {
        emailTesteService.enviar(tipo, request.email(), request.nome());
        return ResponseEntity.ok(new EmailTesteResponse("E-mail de teste enviado com sucesso."));
    }

    public record EmailTesteRequest(
            @NotBlank @Email String email,
            @NotBlank String nome
    ) {}

    public record EmailTesteResponse(String mensagem) {}
}
