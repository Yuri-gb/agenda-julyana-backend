package br.com.agendajulyana.servico.controller;

import br.com.agendajulyana.servico.dto.*;
import br.com.agendajulyana.servico.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categorias")
@Tag(name="Admin — Categorias", description="Gerenciamento administrativo de categorias de serviços.")
@SecurityRequirement(name="bearerAuth")
public class AdminCategoriaController {

    private final CategoriaService service;

    public AdminCategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary="Criar categoria")
    public org.springframework.http.ResponseEntity<CategoriaResponse> criar(Authentication auth, @Valid @RequestBody CriarCategoriaRequest request) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.criar(auth.getName(), request));
    }

    @GetMapping
    @Operation(summary="Listar categorias")
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary="Consultar categoria")
    public CategoriaResponse buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary="Atualizar categoria")
    public CategoriaResponse atualizar(Authentication auth, @PathVariable UUID id,
                                      @Valid @RequestBody AtualizarCategoriaRequest request) {
        return service.atualizar(auth.getName(), id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary="Alterar status da categoria")
    public CategoriaResponse alterarStatus(Authentication auth, @PathVariable UUID id,
                                           @Valid @RequestBody AlterarStatusCategoriaRequest request) {
        return service.alterarStatus(auth.getName(), id, request);
    }
}
