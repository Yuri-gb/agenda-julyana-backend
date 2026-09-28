package br.com.agendajulyana.agendamento.controller;
import br.com.agendajulyana.agendamento.dto.*; import br.com.agendajulyana.agendamento.service.AgendamentoService; import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.security.SecurityRequirement; import io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController @RequestMapping("/api/agendamentos")
@Tag(name="Cliente — Agendamentos", description="Criação, reagendamento e cancelamento de agendamentos pelo cliente.")
@SecurityRequirement(name="bearerAuth")
public class AgendamentoController{private final AgendamentoService service; public AgendamentoController(AgendamentoService s){service=s;}
 @PostMapping @Operation(summary="Criar agendamento") public ResponseEntity<AgendamentoResponse> criar(Authentication a,@Valid @RequestBody CriarAgendamentoRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(UUID.fromString(a.getName()),r));}
 @PostMapping("/{id}/reagendar") @Operation(summary="Reagendar atendimento") public AgendamentoResponse reagendar(Authentication a,@PathVariable UUID id,@Valid @RequestBody ReagendarAgendamentoRequest r){return service.reagendar(UUID.fromString(a.getName()),id,r);}
 @PostMapping("/{id}/cancelar") @Operation(summary="Cancelar agendamento") public ResponseEntity<Void> cancelar(Authentication a,@PathVariable UUID id,@RequestBody(required=false) CancelarAgendamentoRequest r){service.cancelarCliente(UUID.fromString(a.getName()),id,r==null?new CancelarAgendamentoRequest(null):r);return ResponseEntity.noContent().build();}
}
@RestController @RequestMapping("/api/admin/agendamentos")
@Tag(name="Admin — Agendamentos", description="Operações administrativas sobre atendimentos e status da agenda.")
@SecurityRequirement(name="bearerAuth")
class AdminAgendamentoController{private final AgendamentoService service; AdminAgendamentoController(AgendamentoService s){service=s;}
 @PostMapping("/{id}/cancelar") public ResponseEntity<Void> cancelar(Authentication a,@PathVariable UUID id,@RequestBody(required=false) CancelarAgendamentoRequest r){service.cancelarAtendente(UUID.fromString(a.getName()),id,r==null?new CancelarAgendamentoRequest(null):r);return ResponseEntity.noContent().build();}
 @PostMapping("/{id}/realizar") @Operation(summary="Marcar atendimento como realizado") public ResponseEntity<Void> realizar(Authentication a,@PathVariable UUID id){service.realizar(UUID.fromString(a.getName()),id);return ResponseEntity.noContent().build();}
 @PostMapping("/{id}/nao-comparecimento") @Operation(summary="Marcar não comparecimento") public ResponseEntity<Void> naoComparecimento(Authentication a,@PathVariable UUID id){service.marcarNaoComparecimento(UUID.fromString(a.getName()),id);return ResponseEntity.noContent().build();}
}
