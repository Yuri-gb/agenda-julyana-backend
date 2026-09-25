package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.dto.*;
import br.com.agendajulyana.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(service.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication){
        return ResponseEntity.ok(service.me(authentication.getName()));
    }

    @PatchMapping("/me")
    public ResponseEntity<MeResponse> atualizarDados(
        Authentication authentication,
        @Valid @RequestBody AtualizarUsuarioRequest request
    ) {
        return ResponseEntity.ok(service.atualizarDados(authentication.getName(), request));
    }
}
