package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.controller.EmailTesteTipo;
import br.com.agendajulyana.auth.service.email.AgendaEmailRenderer;
import br.com.agendajulyana.auth.service.email.EmailData.EmailAppointmentData;
import br.com.agendajulyana.auth.service.email.EmailData.EmailRefundData;
import br.com.agendajulyana.auth.service.email.EmailData.EmailStatusStep;
import br.com.agendajulyana.auth.service.email.EmailData.PaymentType;
import br.com.agendajulyana.integration.gmail.GmailApiService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class EmailTesteService {
    private static final String CODIGO_TESTE = "483921";
    private static final String CODIGO_AGENDAMENTO_TESTE = "AGD-TESTE-001";

    private final GmailApiService gmailApiService;
    private final AgendaEmailRenderer renderer;

    public EmailTesteService(GmailApiService gmailApiService, AgendaEmailRenderer renderer) {
        this.gmailApiService = gmailApiService;
        this.renderer = renderer;
    }

    public void enviar(EmailTesteTipo tipo, String email, String nome) {
        if (tipo == EmailTesteTipo.RECUPERACAO_SENHA) {
            gmailApiService.enviar(
                    email, nome,
                    "Agenda Julyana — recuperação de senha",
                    renderer.recuperarSenha(nome, CODIGO_TESTE),
                    "Olá, %s! Seu código para redefinir a senha da Agenda Julyana é: %s"
                            .formatted(nome, CODIGO_TESTE)
            );
            return;
        }

        var atendimento = atendimento(nome);

        switch (tipo) {
            case LEMBRETE -> enviar(email, nome, "Agenda Julyana — seu atendimento está chegando", renderer.lembrete(atendimento));
            case AGENDAMENTO_CONFIRMADO -> enviar(email, nome, "Agenda Julyana — agendamento confirmado", renderer.agendamentoConfirmado(atendimento));
            case PAGAMENTO_APROVADO -> enviar(email, nome, "Agenda Julyana — pagamento aprovado", renderer.pagamentoAprovado(atendimento));
            case REAGENDAMENTO -> enviar(email, nome, "Agenda Julyana — atendimento reagendado", renderer.reagendamento(atendimento));
            case CANCELAMENTO -> enviar(email, nome, "Agenda Julyana — atendimento cancelado", renderer.cancelamento(atendimento));
            case REEMBOLSO_INICIADO -> enviar(email, nome, "Agenda Julyana — reembolso iniciado", renderer.reembolsoIniciado(reembolso(atendimento)));
            case REEMBOLSO_CONCLUIDO -> enviar(email, nome, "Agenda Julyana — reembolso concluído", renderer.reembolsoConcluido(reembolso(atendimento)));
            case RECUPERACAO_SENHA -> throw new IllegalStateException("Tipo de recuperação já tratado.");
        }
    }

    private void enviar(String email, String nome, String assunto, String html) {
        gmailApiService.enviar(email, nome, assunto, html, "E-mail de teste da Agenda Julyana.");
    }

    private EmailAppointmentData atendimento(String nome) {
        return new EmailAppointmentData(
                nome, "Massagem relaxante", "Um momento de cuidado, relaxamento e bem-estar.",
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), 60, CODIGO_AGENDAMENTO_TESTE,
                new BigDecimal("180.00"), new BigDecimal("90.00"), new BigDecimal("90.00"),
                PaymentType.ENTRADA
        );
    }

    private EmailRefundData reembolso(EmailAppointmentData atendimento) {
        return new EmailRefundData(
                atendimento, new BigDecimal("90.00"), LocalDateTime.now(),
                List.of(
                        new EmailStatusStep("Solicitado", "Reembolso solicitado.", "icon-reembolso-iniciado", true),
                        new EmailStatusStep("Em processamento", "A operadora está processando.", "icon-horario", true),
                        new EmailStatusStep("Concluído", "Valor devolvido.", "icon-reembolso-concluido", true)
                ),
                "Mesmo meio de pagamento"
        );
    }
}
