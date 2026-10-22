package br.com.agendajulyana.pagamento.service;

import br.com.agendajulyana.agendamento.domain.AgendamentoStatus;
import br.com.agendajulyana.agendamento.domain.ReservaStatus;
import br.com.agendajulyana.agendamento.domain.ReservaTemporaria;
import br.com.agendajulyana.agendamento.repository.AgendamentoRepository;
import br.com.agendajulyana.agendamento.repository.ReservaTemporariaRepository;
import br.com.agendajulyana.pagamento.domain.Pagamento;
import br.com.agendajulyana.pagamento.domain.PagamentoStatus;
import br.com.agendajulyana.pagamento.dto.CheckoutPagamentoResponse;
import br.com.agendajulyana.pagamento.integration.MercadoPagoClient;
import br.com.agendajulyana.pagamento.repository.PagamentoRepository;
import br.com.agendajulyana.pagamento.domain.TentativaPagamento;
import br.com.agendajulyana.pagamento.repository.TentativaPagamentoRepository;
import br.com.agendajulyana.pagamento.repository.ReembolsoRepository;
import br.com.agendajulyana.pagamento.domain.Reembolso;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.agendajulyana.auth.service.email.AgendaNotificationEmailService;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PagamentoService {
    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    private final AgendamentoRepository agendamentos;
    private final ReservaTemporariaRepository reservas;
    private final PagamentoRepository pagamentos;
    private final MercadoPagoClient mercadoPago;
    private final TentativaPagamentoRepository tentativas;
    private final ReembolsoRepository reembolsos;

    @Autowired(required = false)
    private AgendaNotificationEmailService emailService;

    public PagamentoService(
            AgendamentoRepository agendamentos,
            ReservaTemporariaRepository reservas,
            PagamentoRepository pagamentos,
            MercadoPagoClient mercadoPago,
            TentativaPagamentoRepository tentativas,
            ReembolsoRepository reembolsos
    ) {
        this.agendamentos = agendamentos;
        this.reservas = reservas;
        this.pagamentos = pagamentos;
        this.mercadoPago = mercadoPago;
        this.tentativas = tentativas;
        this.reembolsos = reembolsos;
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

        if (pagamento.getStatus() == PagamentoStatus.RECUSADO) {
            pagamento.iniciarNovaTentativa();
        } else if (pagamento.getStatus() != PagamentoStatus.PENDENTE) {
            throw new IllegalStateException("O pagamento não está pendente.");
        }

        if (pagamento.getReferenciaExterna() != null && pagamento.getCheckoutUrl() != null) {
            return resposta(pagamento);
        }

        var pagamentoPersistido = pagamentos.save(pagamento);
        if (pagamentoPersistido == null) pagamentoPersistido = pagamento;
        pagamento = pagamentoPersistido;
        var idempotencyKey = pagamento.getId() != null ? pagamento.getId() :
                (agendamento.getId() != null ? agendamento.getId() : UUID.randomUUID());
        var order = mercadoPago.criarOrder(agendamento, pagamento.getValor(), idempotencyKey);
        if (order == null || order.id() == null || order.checkout_url() == null) {
            throw new IllegalStateException("Mercado Pago não retornou uma ordem válida.");
        }

        pagamento.registrarOrder(order.id(), order.checkout_url());
        pagamentos.save(pagamento);
        tentativas.save(new TentativaPagamento(reserva, pagamento, order.id()));

        return resposta(pagamento);
    }


    @Transactional
    public void processarWebhookOrder(String orderId) {
        log.info("Webhook de pagamento recebido: orderId={}", orderId);
        var order = mercadoPago.consultarOrder(orderId);
        if (order == null || order.id() == null || !order.id().equals(orderId)) {
            throw new IllegalStateException("Order inválida.");
        }

        var pagamento = pagamentos.findByReferenciaExterna(orderId)
                .orElseThrow(() -> new IllegalStateException("Pagamento da order não encontrado."));

        if (order.totalAmount() != null && order.totalAmount().compareTo(pagamento.getValor()) != 0) {
            throw new IllegalStateException("Valor da order diverge do pagamento.");
        }

        var agendamento = pagamento.getAgendamento();
        var reserva = reservas.findByAgendamentoId(agendamento.getId()).orElse(null);

        switch (order.status()) {
            case "processed" -> processarAprovado(pagamento, agendamento, reserva, orderId);
            case "failed" -> {
                if (pagamento.getStatus() == PagamentoStatus.PENDENTE) {
                    pagamento.recusar(orderId);
                    log.info("Pagamento recusado: orderId={}, pagamentoId={}", orderId, pagamento.getId());
                }
            }
            case "canceled", "expired" -> {
                if (pagamento.getStatus() == PagamentoStatus.PENDENTE) pagamento.cancelar(orderId);
                log.info("Order encerrada sem aprovação: orderId={}, status={}", orderId, order.status());
                if (reserva != null && reserva.getStatus() == ReservaStatus.ATIVA) reserva.expirar();
                if (agendamento.getStatus() == AgendamentoStatus.AGUARDANDO_PAGAMENTO) agendamento.cancelar();
            }
            default -> {
                // created/action_required: nenhuma transição final é aplicada.
            }
        }
    }

    private void processarAprovado(Pagamento pagamento, br.com.agendajulyana.agendamento.domain.Agendamento agendamento,
                                   ReservaTemporaria reserva, String orderId) {
        if (pagamento.getStatus() != PagamentoStatus.PENDENTE) return;

        pagamento.aprovar(orderId);

        var reservaValida = reserva != null
                && reserva.getStatus() == ReservaStatus.ATIVA
                && !reserva.estaExpirada(OffsetDateTime.now());

        if (reservaValida) {
            agendamento.confirmar();
            return;
        }

        if (agendamento.getStatus() == AgendamentoStatus.AGUARDANDO_PAGAMENTO) {
            agendamento.cancelar();
        }

        solicitarReembolso(pagamento, null, pagamento.getValor(),
                "Pagamento aprovado após expiração da reserva temporária.");
    }

    @Transactional
    public Reembolso solicitarReembolso(Pagamento pagamento,
                                        br.com.agendajulyana.agendamento.domain.Cancelamento cancelamento,
                                        java.math.BigDecimal valor,
                                        String motivo) {
        var ativos = java.util.List.of(
                br.com.agendajulyana.pagamento.domain.ReembolsoStatus.SOLICITADO,
                br.com.agendajulyana.pagamento.domain.ReembolsoStatus.PROCESSANDO,
                br.com.agendajulyana.pagamento.domain.ReembolsoStatus.CONCLUIDO
        );
        var existente = reembolsos.findFirstByPagamentoIdAndStatusInOrderBySolicitadoEmDesc(pagamento.getId(), ativos);
        if (existente.isPresent()) return existente.get();

        var reembolso = reembolsos.save(new Reembolso(pagamento, cancelamento, valor, motivo));
        try {
            reembolso.marcarProcessando();
            if (emailService != null) emailService.reembolsoIniciado(pagamento.getAgendamento(), pagamento, valor);
            boolean total = valor.compareTo(pagamento.getValor()) == 0;
            String transactionId = null;
            if (!total) {
                var order = mercadoPago.consultarOrder(pagamento.getReferenciaExterna());
                if (order == null || order.transactions() == null || order.transactions().payments() == null
                        || order.transactions().payments().isEmpty()) {
                    throw new IllegalStateException("Mercado Pago não retornou a transação da order.");
                }
                transactionId = order.transactions().payments().get(0).id();
            }
            var refund = mercadoPago.reembolsarOrder(
                    pagamento.getReferenciaExterna(),
                    transactionId,
                    valor,
                    total,
                    reembolso.getId() != null ? reembolso.getId() : UUID.randomUUID()
            );
            if (refund == null || refund.transactions() == null || refund.transactions().refunds() == null
                    || refund.transactions().refunds().isEmpty() || refund.transactions().refunds().get(0).id() == null) {
                throw new IllegalStateException("Mercado Pago não confirmou o reembolso.");
            }
            reembolso.concluir(refund.transactions().refunds().get(0).id());
            var salvo = reembolsos.save(reembolso);
            if (emailService != null) emailService.reembolsoConcluido(pagamento.getAgendamento(), pagamento, valor);
            return salvo;
        } catch (RuntimeException ex) {
            reembolso.falhar();
            reembolsos.save(reembolso);
            throw ex;
        }
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
