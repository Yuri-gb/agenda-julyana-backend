package br.com.agendajulyana.agendamento.controller;
import br.com.agendajulyana.agendamento.dto.*; import br.com.agendajulyana.agendamento.service.AgendamentoService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController @RequestMapping("/api/agendamentos")
public class AgendamentoController{private final AgendamentoService service; public AgendamentoController(AgendamentoService s){service=s;}
 @PostMapping public ResponseEntity<AgendamentoResponse> criar(Authentication a,@Valid @RequestBody CriarAgendamentoRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(UUID.fromString(a.getName()),r));}
 @PostMapping("/{id}/reagendar") public AgendamentoResponse reagendar(Authentication a,@PathVariable UUID id,@Valid @RequestBody ReagendarAgendamentoRequest r){return service.reagendar(UUID.fromString(a.getName()),id,r);}
 @PostMapping("/{id}/cancelar") public ResponseEntity<Void> cancelar(Authentication a,@PathVariable UUID id,@RequestBody(required=false) CancelarAgendamentoRequest r){service.cancelarCliente(UUID.fromString(a.getName()),id,r==null?new CancelarAgendamentoRequest(null):r);return ResponseEntity.noContent().build();}
}
@RestController @RequestMapping("/api/admin/agendamentos")
class AdminAgendamentoController{private final AgendamentoService service; AdminAgendamentoController(AgendamentoService s){service=s;}
 @PostMapping("/{id}/cancelar") public ResponseEntity<Void> cancelar(Authentication a,@PathVariable UUID id,@RequestBody(required=false) CancelarAgendamentoRequest r){service.cancelarAtendente(UUID.fromString(a.getName()),id,r==null?new CancelarAgendamentoRequest(null):r);return ResponseEntity.noContent().build();}
}
