package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.dto.*;
import br.com.agendajulyana.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name="Autenticação", description="Cadastro, login e identidade da conta autenticada.")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}

    @PostMapping("/register")
    @Operation(summary="Cadastrar conta")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @PostMapping("/login")
    @Operation(summary="Entrar com e-mail e senha")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(service.login(request));
    }

    @GetMapping("/me")
    @Operation(summary="Consultar minha conta")
    @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<MeResponse> me(Authentication authentication){
        return ResponseEntity.ok(service.me(authentication.getName()));
    }

    @PatchMapping("/me")
    @Operation(summary="Atualizar meus dados")
    @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<MeResponse> atualizarDados(
        Authentication authentication,
        @Valid @RequestBody AtualizarUsuarioRequest request
    ) {
        return ResponseEntity.ok(service.atualizarDados(authentication.getName(), request));
    }
}
