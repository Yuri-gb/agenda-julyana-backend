package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.service.EmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/emails")
@ConditionalOnProperty(name = "app.email.test-endpoint.enabled", havingValue = "true")
public class EmailTesteController {

    private final EmailService emailService;

    public EmailTesteController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<EmailTesteResponse> enviar(@Valid @RequestBody EmailTesteRequest request) {
        switch (request.tipo()) {
            case RECUPERACAO_SENHA -> emailService.enviarCodigoRecuperacao(
                    request.email(),
                    request.nome(),
                    request.codigo()
            );
        }

        return ResponseEntity.ok(new EmailTesteResponse("E-mail de teste enviado com sucesso."));
    }

    public enum Tipo {
        RECUPERACAO_SENHA
    }

    public record EmailTesteRequest(
            @NotBlank @Email String email,
            @NotNull Tipo tipo,
            @NotBlank String nome,
            @NotBlank String codigo
    ) {
    }

    public record EmailTesteResponse(String mensagem) {
    }
}
