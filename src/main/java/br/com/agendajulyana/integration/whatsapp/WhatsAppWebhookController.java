package br.com.agendajulyana.integration.whatsapp;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/whatsapp")
@Hidden
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final String verifyToken;

    public WhatsAppWebhookController(
            @Value("${app.whatsapp.webhook.verify-token:}") String verifyToken
    ) {
        this.verifyToken = verifyToken;
    }

    @GetMapping
    public ResponseEntity<String> verificarWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge
    ) {
        if (!"subscribe".equals(mode)
                || verifyToken.isBlank()
                || !verifyToken.equals(token)
                || challenge == null
                || challenge.isBlank()) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(challenge);
    }

    @PostMapping
    public ResponseEntity<Void> receberEvento(@RequestBody String payload) {
        log.debug("Webhook WhatsApp recebido: {}", payload);
        return ResponseEntity.ok().build();
    }
}
