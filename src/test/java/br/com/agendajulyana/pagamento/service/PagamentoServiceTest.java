package br.com.agendajulyana.pagamento.service;

import br.com.agendajulyana.agendamento.domain.Agendamento;
import br.com.agendajulyana.agendamento.domain.ReservaTemporaria;
import br.com.agendajulyana.auth.domain.Cliente;
import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.pagamento.domain.Pagamento;
import br.com.agendajulyana.pagamento.domain.PagamentoModalidade;
import br.com.agendajulyana.pagamento.integration.MercadoPagoClient;
import br.com.agendajulyana.pagamento.integration.MercadoPagoOrderResponse;
import br.com.agendajulyana.pagamento.repository.PagamentoRepository;
import br.com.agendajulyana.agendamento.repository.AgendamentoRepository;
import br.com.agendajulyana.agendamento.repository.ReservaTemporariaRepository;
import br.com.agendajulyana.servico.domain.Servico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock AgendamentoRepository agendamentos;
    @Mock ReservaTemporariaRepository reservas;
    @Mock PagamentoRepository pagamentos;
    @Mock MercadoPagoClient mercadoPago;

    @Test
    void deveCriarCheckoutComValorDaEntrada() {
        var usuarioId = UUID.randomUUID();
        var usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(usuarioId);
        when(usuario.getEmail()).thenReturn("cliente@example.com");

        var cliente = new Cliente(usuario);
        var servico = new Servico("Massagem", "Teste", 60, new BigDecimal("200.00"), null);
        var inicio = OffsetDateTime.now().plusHours(2);
        var agendamento = new Agendamento(cliente, servico, inicio, inicio.plusHours(1));
        var reserva = new ReservaTemporaria(agendamento, OffsetDateTime.now());
        var pagamento = new Pagamento(agendamento, PagamentoModalidade.ENTRADA);

        when(agendamentos.findById(any())).thenReturn(Optional.of(agendamento));
        when(reservas.findByAgendamentoId(any())).thenReturn(Optional.of(reserva));
        when(pagamentos.findByAgendamentoId(any())).thenReturn(Optional.of(pagamento));
        when(mercadoPago.criarOrder(agendamento, new BigDecimal("100.00")))
                .thenReturn(new MercadoPagoOrderResponse("ORDER-123", "https://mercadopago.test/checkout/123", "created", "created"));

        var service = new PagamentoService(agendamentos, reservas, pagamentos, mercadoPago);
        var response = service.criarCheckout(usuarioId, UUID.randomUUID());

        assertEquals(new BigDecimal("100.00"), response.valor());
        assertEquals(PagamentoModalidade.ENTRADA, response.modalidade());
        assertEquals("ORDER-123", response.orderId());
        assertEquals("https://mercadopago.test/checkout/123", response.checkoutUrl());
        assertEquals("ORDER-123", pagamento.getReferenciaExterna());
        assertEquals("https://mercadopago.test/checkout/123", pagamento.getCheckoutUrl());
        verify(pagamentos).save(pagamento);
    }

    @Test
    void deveReutilizarCheckoutJaCriado() {
        var usuarioId = UUID.randomUUID();
        var usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(usuarioId);

        var cliente = new Cliente(usuario);
        var servico = new Servico("Massagem", "Teste", 60, new BigDecimal("200.00"), null);
        var inicio = OffsetDateTime.now().plusHours(2);
        var agendamento = new Agendamento(cliente, servico, inicio, inicio.plusHours(1));
        var reserva = new ReservaTemporaria(agendamento, OffsetDateTime.now());
        var pagamento = new Pagamento(agendamento, PagamentoModalidade.PAGAMENTO_TOTAL);
        pagamento.registrarOrder("ORDER-456", "https://mercadopago.test/checkout/456");

        var id = UUID.randomUUID();
        when(agendamentos.findById(id)).thenReturn(Optional.of(agendamento));
        when(reservas.findByAgendamentoId(id)).thenReturn(Optional.of(reserva));
        when(pagamentos.findByAgendamentoId(id)).thenReturn(Optional.of(pagamento));

        var service = new PagamentoService(agendamentos, reservas, pagamentos, mercadoPago);
        var response = service.criarCheckout(usuarioId, id);

        assertEquals(new BigDecimal("200.00"), response.valor());
        assertEquals("ORDER-456", response.orderId());
        verifyNoInteractions(mercadoPago);
        verify(pagamentos, never()).save(any());
    }

    @Test
    void deveRecusarCheckoutDeAgendamentoDeOutroCliente() {
        var donoId = UUID.randomUUID();
        var outroId = UUID.randomUUID();
        var dono = mock(Usuario.class);
        var outro = mock(Usuario.class);
        when(dono.getId()).thenReturn(donoId);
        when(outro.getId()).thenReturn(outroId);

        var cliente = new Cliente(dono);
        var servico = new Servico("Massagem", "Teste", 60, new BigDecimal("200.00"), null);
        var inicio = OffsetDateTime.now().plusHours(2);
        var agendamento = new Agendamento(cliente, servico, inicio, inicio.plusHours(1));

        var id = UUID.randomUUID();
        when(agendamentos.findById(id)).thenReturn(Optional.of(agendamento));

        var service = new PagamentoService(agendamentos, reservas, pagamentos, mercadoPago);

        assertThrows(IllegalStateException.class, () -> service.criarCheckout(outroId, id));
        verifyNoInteractions(mercadoPago);
    }
}
