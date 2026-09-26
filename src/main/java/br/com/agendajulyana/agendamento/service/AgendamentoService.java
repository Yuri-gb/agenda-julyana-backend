package br.com.agendajulyana.agendamento.service;

import br.com.agendajulyana.agendamento.domain.*;
import br.com.agendajulyana.agendamento.dto.*;
import br.com.agendajulyana.agendamento.repository.*;
import br.com.agendajulyana.auth.domain.Cliente;
import br.com.agendajulyana.auth.repository.ClienteRepository;
import br.com.agendajulyana.disponibilidade.repository.*;
import br.com.agendajulyana.servico.domain.ServicoStatus;
import br.com.agendajulyana.servico.repository.ServicoRepository;
import br.com.agendajulyana.auditoria.domain.Auditoria;
import br.com.agendajulyana.auditoria.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*; import java.util.*;

@Service
public class AgendamentoService {
 private final AgendamentoRepository agendamentos; private final ReservaTemporariaRepository reservas; private final ClienteRepository clientes; private final ServicoRepository servicos;
 private final DisponibilidadeRepository disponibilidades; private final BloqueioRepository bloqueios; private final IndisponibilidadeServicoRepository indisponibilidades; private final AuditoriaRepository auditorias;
 private final ReagendamentoRepository reagendamentos; private final CancelamentoRepository cancelamentos;
 public AgendamentoService(AgendamentoRepository a,ReservaTemporariaRepository r,ClienteRepository c,ServicoRepository s,DisponibilidadeRepository d,BloqueioRepository b,IndisponibilidadeServicoRepository i,AuditoriaRepository au,ReagendamentoRepository re,CancelamentoRepository ca){agendamentos=a;reservas=r;clientes=c;servicos=s;disponibilidades=d;bloqueios=b;indisponibilidades=i;auditorias=au;reagendamentos=re;cancelamentos=ca;}
 @Transactional public AgendamentoResponse criar(UUID usuarioId,CriarAgendamentoRequest req){
  Cliente cliente=clientes.findByUsuarioId(usuarioId).orElseThrow(()->new IllegalStateException("Perfil de cliente não encontrado."));
  var servico=servicos.findById(req.servicoId()).orElseThrow(()->new IllegalArgumentException("Serviço não encontrado."));
  if(servico.getStatus()!=ServicoStatus.ATIVO)throw new IllegalStateException("Serviço indisponível.");
  var inicio=req.inicio(); var fim=inicio.plusMinutes(servico.getDuracaoMinutos()); var agora=OffsetDateTime.now();
  if(inicio.isBefore(agora))throw new IllegalArgumentException("O horário deve ser futuro.");
  validarDisponibilidade(servico.getId(),inicio,fim);
  if(agendamentos.existeConflito(inicio,fim,List.of(AgendamentoStatus.AGUARDANDO_PAGAMENTO,AgendamentoStatus.CONFIRMADO)))throw new IllegalStateException("Horário já reservado.");
  var a=agendamentos.save(new Agendamento(cliente,servico,inicio,fim)); var r=reservas.save(new ReservaTemporaria(a,agora));
  auditar(usuarioId,"CRIAR","AGENDAMENTO",a.getId()); return response(a,r);
 }
 @Transactional public AgendamentoResponse expirarReserva(UUID usuarioId,UUID agendamentoId){
  var a=obter(agendamentoId); var r=reservas.findAll().stream().filter(x->x.getAgendamento().getId().equals(agendamentoId)).findFirst().orElseThrow();
  if(r.getStatus()==ReservaStatus.ATIVA && r.estaExpirada(OffsetDateTime.now())){r.expirar();a.cancelar();auditar(usuarioId,"EXPIRAR_RESERVA","AGENDAMENTO",a.getId());} return response(a,r);
 }
 @Transactional public AgendamentoResponse reagendar(UUID usuarioId,UUID id,ReagendarAgendamentoRequest req){
  var a=obter(id); if(a.getStatus()!=AgendamentoStatus.CONFIRMADO)throw new IllegalStateException("Apenas agendamento confirmado pode ser reagendado.");
  if(Duration.between(OffsetDateTime.now(),a.getInicio()).toHours()<24)throw new IllegalStateException("Reagendamento exige antecedência mínima de 24 horas.");
  long total=reagendamentos.countByAgendamentoId(id); if(total>=2)throw new IllegalStateException("Limite de 2 reagendamentos atingido.");
  var novo=req.inicio(); if(novo.isBefore(OffsetDateTime.now()))throw new IllegalArgumentException("O novo horário deve ser futuro.");
  var fim=novo.plusMinutes(a.getDuracaoMinutos()); validarDisponibilidade(a.getServico().getId(),novo,fim);
  if(agendamentos.existeConflito(novo,fim,List.of(AgendamentoStatus.AGUARDANDO_PAGAMENTO,AgendamentoStatus.CONFIRMADO)))throw new IllegalStateException("Novo horário já reservado.");
  var antigo=a.getInicio(); a.reagendar(novo); agendamentos.save(a); reagendamentos.save(new Reagendamento(a,(short)(total+1),antigo,novo,req.motivo()));
  auditar(usuarioId,"REAGENDAR","AGENDAMENTO",id); return response(a,null);
 }
 @Transactional public void cancelarCliente(UUID usuarioId,UUID id,CancelarAgendamentoRequest req){
  var a=obter(id); if(!a.getCliente().getUsuario().getId().equals(usuarioId))throw new IllegalStateException("Agendamento não pertence ao cliente.");
  a.cancelar();agendamentos.save(a);cancelamentos.save(new Cancelamento(a,Cancelamento.OrigemCancelamento.CLIENTE,req.motivo(),usuarioId));auditar(usuarioId,"CANCELAR","AGENDAMENTO",id);
 }
 @Transactional public void cancelarAtendente(UUID usuarioId,UUID id,CancelarAgendamentoRequest req){
  var a=obter(id); a.cancelar();agendamentos.save(a);cancelamentos.save(new Cancelamento(a,Cancelamento.OrigemCancelamento.ATENDENTE,req.motivo(),usuarioId));auditar(usuarioId,"CANCELAR_ATENDENTE","AGENDAMENTO",id);
 }
 private void validarDisponibilidade(UUID servicoId,OffsetDateTime inicio,OffsetDateTime fim){
  int dow=inicio.getDayOfWeek().getValue(); short dia=(short)dow;
  boolean dentro=disponibilidades.findByDiaSemanaAndAtivoTrue(dia).stream().anyMatch(d->!inicio.toLocalTime().isBefore(d.getHoraInicio())&&!fim.toLocalTime().isAfter(d.getHoraFim()));
  if(!dentro)throw new IllegalStateException("Horário fora da disponibilidade.");
  if(bloqueios.existeSobreposicao(inicio,fim,null))throw new IllegalStateException("Horário bloqueado.");
  if(indisponibilidades.existeSobreposicao(servicoId,inicio,fim,null))throw new IllegalStateException("Serviço indisponível neste horário.");
 }
 private Agendamento obter(UUID id){return agendamentos.findById(id).orElseThrow(()->new IllegalArgumentException("Agendamento não encontrado."));}
 private void auditar(UUID u,String acao,String tipo,UUID id){auditorias.save(new Auditoria(u,acao,tipo,id,"SUCESSO",Map.of()));}
 private AgendamentoResponse response(Agendamento a,ReservaTemporaria r){return new AgendamentoResponse(a.getId(),a.getServico().getId(),a.getInicio(),a.getFim(),a.getDuracaoMinutos(),a.getValorServico(),a.getValorEntrada(),a.getStatus().name(),r==null?null:r.getId(),r==null?null:r.getExpiraEm());}
}
