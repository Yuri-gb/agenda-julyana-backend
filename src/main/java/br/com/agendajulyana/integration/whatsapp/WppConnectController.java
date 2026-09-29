package br.com.agendajulyana.integration.whatsapp;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integrations/whatsapp/wppconnect")
@Hidden
public class WppConnectController {
    private final WppConnectClient client;
    public WppConnectController(WppConnectClient client) { this.client = client; }

    @PostMapping("/session/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> startSession() { return ResponseEntity.ok(client.startSession()); }

    @GetMapping("/session/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> statusSession() { return ResponseEntity.ok(client.statusSession()); }
}
