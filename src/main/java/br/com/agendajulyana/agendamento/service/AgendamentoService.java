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
import java.time.*;
import java.util.*;

@Service
public class AgendamentoService {
 private final AgendamentoRepository agendamentos; private final ReservaTemporariaRepository reservas; private final ClienteRepository clientes; private final ServicoRepository servicos;
 private final DisponibilidadeRepository disponibilidades; private final BloqueioRepository bloqueios; private final IndisponibilidadeServicoRepository indisponibilidades; private final AuditoriaRepository auditorias;
 public AgendamentoService(AgendamentoRepository a,ReservaTemporariaRepository r,ClienteRepository c,ServicoRepository s,DisponibilidadeRepository d,BloqueioRepository b,IndisponibilidadeServicoRepository i,AuditoriaRepository au){agendamentos=a;reservas=r;clientes=c;servicos=s;disponibilidades=d;bloqueios=b;indisponibilidades=i;auditorias=au;}
 @Transactional
 public AgendamentoResponse criar(UUID usuarioId,CriarAgendamentoRequest req){
  Cliente cliente=clientes.findByUsuarioId(usuarioId).orElseThrow(()->new IllegalStateException("Perfil de cliente não encontrado."));
  var servico=servicos.findById(req.servicoId()).orElseThrow(()->new IllegalArgumentException("Serviço não encontrado."));
  if(servico.getStatus()!=ServicoStatus.ATIVO)throw new IllegalStateException("Serviço indisponível.");
  var inicio=req.inicio(); var fim=inicio.plusMinutes(servico.getDuracaoMinutos()); var agora=OffsetDateTime.now();
  if(inicio.isBefore(agora))throw new IllegalArgumentException("O horário deve ser futuro.");
  int dow=inicio.atZoneSameInstant(ZoneOffset.UTC).getDayOfWeek().getValue();
  short dia=(short)dow;
  boolean dentro=disponibilidades.findByDiaSemanaAndAtivoTrue(dia).stream().anyMatch(d->!inicio.toLocalTime().isBefore(d.getHoraInicio())&&!fim.toLocalTime().isAfter(d.getHoraFim()));
  if(!dentro)throw new IllegalStateException("Horário fora da disponibilidade.");
  if(bloqueios.existeSobreposicao(inicio,fim,null))throw new IllegalStateException("Horário bloqueado.");
  if(indisponibilidades.existeSobreposicao(servico.getId(),inicio,fim,null))throw new IllegalStateException("Serviço indisponível neste horário.");
  if(agendamentos.existeConflito(inicio,fim,List.of(AgendamentoStatus.AGUARDANDO_PAGAMENTO,AgendamentoStatus.CONFIRMADO)))throw new IllegalStateException("Horário já reservado.");
  var a=agendamentos.save(new Agendamento(cliente,servico,inicio,fim));
  var r=reservas.save(new ReservaTemporaria(a,agora));
  auditorias.save(new Auditoria(usuarioId,"CRIAR","AGENDAMENTO",a.getId(),"SUCESSO",Map.of()));
  return response(a,r);
 }
 private AgendamentoResponse response(Agendamento a,ReservaTemporaria r){return new AgendamentoResponse(a.getId(),a.getServico().getId(),a.getInicio(),a.getFim(),a.getDuracaoMinutos(),a.getValorServico(),a.getValorEntrada(),a.getStatus().name(),r.getId(),r.getExpiraEm());}
}
