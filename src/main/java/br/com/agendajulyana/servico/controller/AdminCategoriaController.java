package br.com.agendajulyana.servico.controller;

import br.com.agendajulyana.servico.dto.*;
import br.com.agendajulyana.servico.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categorias")
public class AdminCategoriaController {

    private final CategoriaService service;

    public AdminCategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<CategoriaResponse> criar(Authentication auth, @Valid @RequestBody CriarCategoriaRequest request) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.criar(auth.getName(), request));
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(Authentication auth, @PathVariable UUID id,
                                      @Valid @RequestBody AtualizarCategoriaRequest request) {
        return service.atualizar(auth.getName(), id, request);
    }

    @PatchMapping("/{id}/status")
    public CategoriaResponse alterarStatus(Authentication auth, @PathVariable UUID id,
                                           @Valid @RequestBody AlterarStatusCategoriaRequest request) {
        return service.alterarStatus(auth.getName(), id, request);
    }
}
