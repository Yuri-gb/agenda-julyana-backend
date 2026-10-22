package br.com.agendajulyana.integration.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/whatsapp/wppconnect")
public class WppConnectWebhookController {
    private static final Logger log = LoggerFactory.getLogger(WppConnectWebhookController.class);

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody String payload) {
        log.debug("Webhook WPPConnect recebido.");
        return ResponseEntity.ok().build();
    }
}
