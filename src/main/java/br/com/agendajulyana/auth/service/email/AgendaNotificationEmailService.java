package br.com.agendajulyana.auth.service.email;

import br.com.agendajulyana.agendamento.domain.Agendamento;
import br.com.agendajulyana.auth.service.GmailApiEmailService;
import br.com.agendajulyana.auth.service.EmailTemplate;
import br.com.agendajulyana.integration.gmail.GmailApiService;
import br.com.agendajulyana.pagamento.domain.Pagamento;
import br.com.agendajulyana.pagamento.domain.PagamentoModalidade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static br.com.agendajulyana.auth.service.email.EmailData.*;

@Service
public class AgendaNotificationEmailService {
    private static final Logger log = LoggerFactory.getLogger(AgendaNotificationEmailService.class);

    private final GmailApiService gmail;
    private final AgendaEmailRenderer renderer;

    public AgendaNotificationEmailService(GmailApiService gmail, AgendaEmailRenderer renderer) {
        this.gmail = gmail;
        this.renderer = renderer;
    }

    public void agendamentoConfirmado(Agendamento a, Pagamento p) {
        enviar(a, "agendamento confirmado", "Agendamento confirmado", renderer.agendamentoConfirmado(dados(a, p)));
    }

    public void pagamentoAprovado(Agendamento a, Pagamento p) {
        enviar(a, "pagamento aprovado", "Pagamento aprovado", renderer.pagamentoAprovado(dados(a, p)));
    }

    public void reagendamento(Agendamento a, Pagamento p) {
        enviar(a, "reagendamento", "Atendimento reagendado", renderer.reagendamento(dados(a, p)));
    }

    public void cancelamento(Agendamento a, Pagamento p) {
        enviar(a, "cancelamento", "Atendimento cancelado", renderer.cancelamento(dados(a, p)));
    }

    public void reembolsoIniciado(Agendamento a, Pagamento p, BigDecimal valor) {
        enviar(a, "reembolso iniciado", "Reembolso iniciado",
                renderer.reembolsoIniciado(reembolso(a, p, valor)));
    }

    public void reembolsoConcluido(Agendamento a, Pagamento p, BigDecimal valor) {
        enviar(a, "reembolso concluído", "Reembolso concluído",
                renderer.reembolsoConcluido(reembolso(a, p, valor)));
    }

    private void enviar(Agendamento a, String evento, String assunto, String html) {
        var usuario = a.getCliente().getUsuario();
        try {
            gmail.enviar(usuario.getEmail(), usuario.getNome(), "Agenda Julyana — " + assunto,
                    html, "Notificação da Agenda Julyana.");
            log.info("E-mail de evento enviado: evento={}, agendamentoId={}, destinatario={}",
                    evento, a.getId(), usuario.getEmail());
        } catch (RuntimeException ex) {
            log.error("Falha ao enviar e-mail de evento: evento={}, agendamentoId={}, destinatario={}",
                    evento, a.getId(), usuario.getEmail(), ex);
        }
    }

    private EmailAppointmentData dados(Agendamento a, Pagamento p) {
        var valorServico = a.getValorServico();
        var valorPago = p == null ? BigDecimal.ZERO : p.getValor();
        var restante = valorServico == null ? null : valorServico.subtract(valorPago).max(BigDecimal.ZERO);
        var modalidade = p != null && p.getModalidade() == PagamentoModalidade.PAGAMENTO_TOTAL
                ? PaymentType.TOTAL : PaymentType.ENTRADA;
        return new EmailAppointmentData(
                a.getCliente().getUsuario().getNome(),
                a.getServico().getNome(),
                a.getServico().getDescricao(),
                a.getInicio().toLocalDate(),
                a.getInicio().toLocalTime(),
                a.getDuracaoMinutos(),
                a.getId() == null ? null : a.getId().toString(),
                valorServico,
                valorPago,
                restante,
                modalidade
        );
    }

    private EmailRefundData reembolso(Agendamento a, Pagamento p, BigDecimal valor) {
        return new EmailRefundData(
                dados(a, p),
                valor,
                LocalDateTime.now(),
                List.of(
                        new EmailStatusStep("Solicitado", "Reembolso solicitado.", "icon-reembolso-iniciado", true),
                        new EmailStatusStep("Em processamento", "A operadora está processando.", "icon-horario", true),
                        new EmailStatusStep("Concluído", "Valor devolvido.", "icon-reembolso-concluido", true)
                ),
                "Mesmo meio de pagamento"
        );
    }
}
