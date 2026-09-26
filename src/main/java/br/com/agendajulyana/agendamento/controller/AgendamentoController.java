package br.com.agendajulyana.agendamento.controller;

import br.com.agendajulyana.agendamento.dto.*;
import br.com.agendajulyana.agendamento.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/agendamentos")
public class AgendamentoController{
 private final AgendamentoService service;
 public AgendamentoController(AgendamentoService s){service=s;}
 @PostMapping public AgendamentoResponse criar(Authentication a,@Valid @RequestBody CriarAgendamentoRequest r){return service.criar(UUID.fromString(a.getName()),r);}
}
