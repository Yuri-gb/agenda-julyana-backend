package br.com.agendajulyana.servico.controller;

import br.com.agendajulyana.servico.dto.*;
import br.com.agendajulyana.servico.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/servicos")
public class AdminServicoController {

    private final ServicoService service;

    public AdminServicoController(ServicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(Authentication auth, @Valid @RequestBody CriarServicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(auth.getName(), request));
    }

    @GetMapping
    public List<ServicoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ServicoResponse buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public ServicoResponse atualizar(Authentication auth, @PathVariable UUID id,
                                     @Valid @RequestBody AtualizarServicoRequest request) {
        return service.atualizar(auth.getName(), id, request);
    }

    @PatchMapping("/{id}/status")
    public ServicoResponse alterarStatus(Authentication auth, @PathVariable UUID id,
                                         @Valid @RequestBody AlterarStatusServicoRequest request) {
        return service.alterarStatus(auth.getName(), id, request);
    }
}
