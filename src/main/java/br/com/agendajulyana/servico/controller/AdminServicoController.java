package br.com.agendajulyana.servico.controller;

import br.com.agendajulyana.servico.dto.*;
import br.com.agendajulyana.servico.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/servicos")
@Tag(name="Admin — Serviços", description="Gerenciamento administrativo de serviços.")
@SecurityRequirement(name="bearerAuth")
public class AdminServicoController {

    private final ServicoService service;

    public AdminServicoController(ServicoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary="Criar serviço")
    public ResponseEntity<ServicoResponse> criar(Authentication auth, @Valid @RequestBody CriarServicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(auth.getName(), request));
    }

    @GetMapping
    @Operation(summary="Listar serviços")
    public List<ServicoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary="Consultar serviço")
    public ServicoResponse buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary="Atualizar serviço")
    public ServicoResponse atualizar(Authentication auth, @PathVariable UUID id,
                                     @Valid @RequestBody AtualizarServicoRequest request) {
        return service.atualizar(auth.getName(), id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary="Alterar status do serviço")
    public ServicoResponse alterarStatus(Authentication auth, @PathVariable UUID id,
                                         @Valid @RequestBody AlterarStatusServicoRequest request) {
        return service.alterarStatus(auth.getName(), id, request);
    }
}
