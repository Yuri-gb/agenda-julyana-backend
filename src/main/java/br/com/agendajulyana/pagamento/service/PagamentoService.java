package br.com.agendajulyana.pagamento.service;

import br.com.agendajulyana.agendamento.domain.AgendamentoStatus;
import br.com.agendajulyana.agendamento.domain.ReservaStatus;
import br.com.agendajulyana.agendamento.repository.AgendamentoRepository;
import br.com.agendajulyana.agendamento.repository.ReservaTemporariaRepository;
import br.com.agendajulyana.pagamento.domain.Pagamento;
import br.com.agendajulyana.pagamento.domain.PagamentoStatus;
import br.com.agendajulyana.pagamento.dto.CheckoutPagamentoResponse;
import br.com.agendajulyana.pagamento.integration.MercadoPagoClient;
import br.com.agendajulyana.pagamento.repository.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PagamentoService {

    private final AgendamentoRepository agendamentos;
    private final ReservaTemporariaRepository reservas;
    private final PagamentoRepository pagamentos;
    private final MercadoPagoClient mercadoPago;

    public PagamentoService(
            AgendamentoRepository agendamentos,
            ReservaTemporariaRepository reservas,
            PagamentoRepository pagamentos,
            MercadoPagoClient mercadoPago
    ) {
        this.agendamentos = agendamentos;
        this.reservas = reservas;
        this.pagamentos = pagamentos;
        this.mercadoPago = mercadoPago;
    }

    @Transactional
    public CheckoutPagamentoResponse criarCheckout(UUID usuarioId, UUID agendamentoId) {
        var agendamento = agendamentos.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));

        if (!agendamento.getCliente().getUsuario().getId().equals(usuarioId)) {
            throw new IllegalStateException("Agendamento não pertence ao cliente.");
        }

        if (agendamento.getStatus() != AgendamentoStatus.AGUARDANDO_PAGAMENTO) {
            throw new IllegalStateException("Agendamento não está aguardando pagamento.");
        }

        var reserva = reservas.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new IllegalStateException("Reserva temporária não encontrada."));

        if (reserva.getStatus() != ReservaStatus.ATIVA
                || reserva.estaExpirada(OffsetDateTime.now())) {
            throw new IllegalStateException("A reserva temporária expirou.");
        }

        var pagamento = pagamentos.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new IllegalStateException("Pagamento não encontrado."));

        if (pagamento.getStatus() != PagamentoStatus.PENDENTE) {
            throw new IllegalStateException("O pagamento não está pendente.");
        }

        if (pagamento.getReferenciaExterna() != null && pagamento.getCheckoutUrl() != null) {
            return resposta(pagamento);
        }

        var order = mercadoPago.criarOrder(agendamento, pagamento.getValor());
        if (order == null || order.id() == null || order.checkout_url() == null) {
            throw new IllegalStateException("Mercado Pago não retornou uma ordem válida.");
        }

        pagamento.registrarOrder(order.id(), order.checkout_url());
        pagamentos.save(pagamento);

        return resposta(pagamento);
    }

    private CheckoutPagamentoResponse resposta(Pagamento pagamento) {
        return new CheckoutPagamentoResponse(
                pagamento.getId(),
                pagamento.getAgendamento().getId(),
                pagamento.getModalidade(),
                pagamento.getValor(),
                pagamento.getReferenciaExterna(),
                pagamento.getCheckoutUrl()
        );
    }
}
