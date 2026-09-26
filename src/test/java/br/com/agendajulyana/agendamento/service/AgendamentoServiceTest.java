package br.com.agendajulyana.agendamento.service;

import br.com.agendajulyana.agendamento.domain.*;
import br.com.agendajulyana.agendamento.dto.*;
import br.com.agendajulyana.agendamento.repository.*;
import br.com.agendajulyana.auth.domain.*;
import br.com.agendajulyana.auth.repository.ClienteRepository;
import br.com.agendajulyana.auditoria.repository.AuditoriaRepository;
import br.com.agendajulyana.disponibilidade.repository.*;
import br.com.agendajulyana.servico.domain.*;
import br.com.agendajulyana.servico.repository.ServicoRepository;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import org.mockito.*;
import java.math.BigDecimal; import java.time.*; import java.util.*;
import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AgendamentoServiceTest {
 @Mock AgendamentoRepository agendamentos; @Mock ReservaTemporariaRepository reservas; @Mock ClienteRepository clientes; @Mock ServicoRepository servicos;
 @Mock DisponibilidadeRepository disponibilidades; @Mock BloqueioRepository bloqueios; @Mock IndisponibilidadeServicoRepository indisponibilidades; @Mock AuditoriaRepository auditorias;
 @Mock ReagendamentoRepository reagendamentos; @Mock CancelamentoRepository cancelamentos;
 @Test void deveCriarReservaDe30Minutos(){
  var usuario=new Usuario("Cliente","c@e.com","75999999999"); var cliente=new Cliente(usuario); var servico=new Servico("Teste","x",60,new BigDecimal("100.00"),null);
  var agora=OffsetDateTime.now().plusHours(1).withNano(0);
  when(clientes.findByUsuarioId(any())).thenReturn(Optional.of(cliente)); when(servicos.findById(any())).thenReturn(Optional.of(servico));
  when(disponibilidades.findByDiaSemanaAndAtivoTrue(anyShort())).thenReturn(List.of(mock(br.com.agendajulyana.disponibilidade.domain.Disponibilidade.class)));
  var d=disponibilidades.findByDiaSemanaAndAtivoTrue((short)agora.getDayOfWeek().getValue()).get(0);
  when(d.getHoraInicio()).thenReturn(agora.toLocalTime().minusMinutes(1)); when(d.getHoraFim()).thenReturn(agora.toLocalTime().plusHours(2));
  when(agendamentos.existeConflito(any(),any(),any())).thenReturn(false);
  when(bloqueios.existeSobreposicao(any(),any(),isNull())).thenReturn(false); when(indisponibilidades.existeSobreposicao(any(),any(),any(),isNull())).thenReturn(false);
  var a=new Agendamento(cliente,servico,agora,agora.plusHours(1)); when(agendamentos.save(any())).thenReturn(a);
  var r=new ReservaTemporaria(a,OffsetDateTime.now()); when(reservas.save(any())).thenReturn(r);
  var s=new AgendamentoService(agendamentos,reservas,clientes,servicos,disponibilidades,bloqueios,indisponibilidades,auditorias,reagendamentos,cancelamentos);
  var out=s.criar(UUID.randomUUID(),new CriarAgendamentoRequest(UUID.randomUUID(),agora));
  assertEquals("AGUARDANDO_PAGAMENTO",out.status()); assertNotNull(out.reservaExpiraEm()); assertTrue(out.reservaExpiraEm().isAfter(out.inicio()));
 }
 @Test void deveRecusarConflito(){
  var usuario=new Usuario("Cliente","c@e.com","75999999999"); var cliente=new Cliente(usuario); var servico=new Servico("Teste","x",60,new BigDecimal("100.00"),null);
  var inicio=OffsetDateTime.now().plusHours(2);
  when(clientes.findByUsuarioId(any())).thenReturn(Optional.of(cliente)); when(servicos.findById(any())).thenReturn(Optional.of(servico));
  var d=mock(br.com.agendajulyana.disponibilidade.domain.Disponibilidade.class); when(d.getHoraInicio()).thenReturn(inicio.toLocalTime().minusMinutes(1)); when(d.getHoraFim()).thenReturn(inicio.toLocalTime().plusHours(2));
  when(disponibilidades.findByDiaSemanaAndAtivoTrue(anyShort())).thenReturn(List.of(d)); when(bloqueios.existeSobreposicao(any(),any(),isNull())).thenReturn(false); when(indisponibilidades.existeSobreposicao(any(),any(),any(),isNull())).thenReturn(false);
  when(agendamentos.existeConflito(any(),any(),any())).thenReturn(true);
  var s=new AgendamentoService(agendamentos,reservas,clientes,servicos,disponibilidades,bloqueios,indisponibilidades,auditorias,reagendamentos,cancelamentos);
  assertThrows(IllegalStateException.class,()->s.criar(UUID.randomUUID(),new CriarAgendamentoRequest(UUID.randomUUID(),inicio)));
 }
}
