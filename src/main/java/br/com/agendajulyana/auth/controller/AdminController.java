package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.dto.CriarAdminRequest;
import br.com.agendajulyana.auth.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name="Admin — Usuários", description="Operações administrativas relacionadas a usuários.")
@SecurityRequirement(name="bearerAuth")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @PostMapping("/usuarios")
    @Operation(summary="Criar administrador")
    public ResponseEntity<Void> criarAdmin(
        Authentication authentication,
        @Valid @RequestBody CriarAdminRequest request
    ) {
        service.criarAdmin(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }
}
