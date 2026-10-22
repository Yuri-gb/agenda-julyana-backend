package br.com.agendajulyana.agendamento.controller;

import br.com.agendajulyana.agendamento.dto.*;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.agendamento.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")
@Tag(name="Cliente — Agendamentos", description="Criação, reagendamento e cancelamento de agendamentos pelo cliente.")
@SecurityRequirement(name="bearerAuth")
public class AgendamentoController {
    private final AgendamentoService service;
    private final UsuarioRepository usuarios;

    public AgendamentoController(AgendamentoService service, UsuarioRepository usuarios) {
        this.service = service;
        this.usuarios = usuarios;
    }

    private UUID usuarioId(Authentication authentication) {
        return usuarios.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."))
                .getId();
    }

    @PostMapping
    @Operation(summary="Criar agendamento")
    public ResponseEntity<AgendamentoResponse> criar(
            Authentication authentication,
            @Valid @RequestBody CriarAgendamentoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(usuarioId(authentication), request));
    }

    @PostMapping("/{id}/reagendar")
    @Operation(summary="Reagendar atendimento")
    public AgendamentoResponse reagendar(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody ReagendarAgendamentoRequest request
    ) {
        return service.reagendar(usuarioId(authentication), id, request);
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary="Cancelar agendamento")
    public ResponseEntity<Void> cancelar(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody(required=false) CancelarAgendamentoRequest request
    ) {
        service.cancelarCliente(usuarioId(authentication), id,
                request == null ? new CancelarAgendamentoRequest(null) : request);
        return ResponseEntity.noContent().build();
    }
}

@RestController
@RequestMapping("/api/admin/agendamentos")
@Tag(name="Admin — Agendamentos", description="Operações administrativas sobre atendimentos e status da agenda.")
@SecurityRequirement(name="bearerAuth")
class AdminAgendamentoController {
    private final AgendamentoService service;
    private final UsuarioRepository usuarios;

    AdminAgendamentoController(AgendamentoService service, UsuarioRepository usuarios) {
        this.service = service;
        this.usuarios = usuarios;
    }

    private UUID usuarioId(Authentication authentication) {
        return usuarios.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."))
                .getId();
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody(required=false) CancelarAgendamentoRequest request
    ) {
        service.cancelarAtendente(usuarioId(authentication), id,
                request == null ? new CancelarAgendamentoRequest(null) : request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/realizar")
    @Operation(summary="Marcar atendimento como realizado")
    public ResponseEntity<Void> realizar(Authentication authentication, @PathVariable UUID id) {
        service.realizar(usuarioId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/nao-comparecimento")
    @Operation(summary="Marcar não comparecimento")
    public ResponseEntity<Void> naoComparecimento(Authentication authentication, @PathVariable UUID id) {
        service.marcarNaoComparecimento(usuarioId(authentication), id);
        return ResponseEntity.noContent().build();
    }
}