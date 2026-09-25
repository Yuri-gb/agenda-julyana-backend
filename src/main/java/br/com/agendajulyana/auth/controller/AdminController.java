package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.dto.CriarAdminRequest;
import br.com.agendajulyana.auth.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @PostMapping("/usuarios")
    public ResponseEntity<Void> criarAdmin(
        Authentication authentication,
        @Valid @RequestBody CriarAdminRequest request
    ) {
        service.criarAdmin(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }
}
