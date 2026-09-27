package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.service.email.EmailData.EmailAppointmentData;
import br.com.agendajulyana.auth.service.email.EmailData.EmailRefundData;
import br.com.agendajulyana.auth.service.email.EmailData.EmailStatusStep;
import br.com.agendajulyana.auth.service.email.EmailData.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateTest {
    private final EmailTemplate template = new EmailTemplate();

    private EmailAppointmentData atendimento(PaymentType tipo) {
        return new EmailAppointmentData(
                "Maria", "Massagem Relaxante", "Bem-estar e alívio das tensões do dia a dia.",
                LocalDate.of(2026, 10, 22), LocalTime.of(14, 0), 90,
                "JL R 001", new BigDecimal("200.00"), new BigDecimal("100.00"),
                new BigDecimal("100.00"), tipo);
    }

    @Test
    void deveGerarTemplateDeRecuperacaoComEstruturaVisualBase() {
        var html = template.recuperarSenha("Maria", "483921");
        assertAll(
                () -> assertTrue(html.contains("cid:julyana-email-header")),
                () -> assertTrue(html.contains("Olá!")),
                () -> assertTrue(html.contains("483921")),
                () -> assertTrue(html.contains("15 minutos")),
                () -> assertTrue(html.contains("uma única vez")),
                () -> assertTrue(html.contains("Julyana Lima — Estética e Bem-estar")),
                () -> assertTrue(html.contains("Siga nossas redes")),
                () -> assertTrue(html.contains("Feira de Santana - BA"))
        );
    }

    @Test
    void devePrepararAssetsEPlaceholders() {
        var html = template.recuperarSenha("Maria", "483921");
        assertAll(
                () -> assertTrue(html.contains("data-asset='icon-copy'")),
                () -> assertTrue(html.contains("data-asset='icon-lock'")),
                () -> assertTrue(html.contains("data-asset='icon-horario'")),
                () -> assertTrue(html.contains("data-asset='icon-seguranca'")),
                () -> assertTrue(html.contains("data-asset='icon-assinatura'")),
                () -> assertTrue(html.contains("cid:footer-background"))
        );
    }

    @Test
    void deveManterVariacaoDePagamentoNoMesmoTemplate() {
        var total = template.agendamentoConfirmado(atendimento(PaymentType.TOTAL));
        var entrada = template.agendamentoConfirmado(atendimento(PaymentType.ENTRADA));
        assertAll(
                () -> assertTrue(total.contains("Pagamento realizado")),
                () -> assertTrue(total.contains("Valor total do serviço")),
                () -> assertTrue(entrada.contains("Entrada paga")),
                () -> assertTrue(entrada.contains("Restante no atendimento")),
                () -> assertTrue(entrada.contains("50% do valor do serviço"))
        );
    }

    @Test
    void deveGerarTodosOsTemplatesPrevistos() {
        var d = atendimento(PaymentType.ENTRADA);
        var refund = new EmailRefundData(d, new BigDecimal("100.00"), LocalDateTime.of(2026, 10, 15, 16, 42), List.of(
                new EmailStatusStep("Solicitação recebida", "Recebemos a solicitação.", "icon-copy", true),
                new EmailStatusStep("Em processamento", "O reembolso foi enviado.", "icon-copy", true),
                new EmailStatusStep("Estorno realizado", "O valor foi devolvido.", "icon-copy", true),
                new EmailStatusStep("Reembolso concluído", "O reembolso foi concluído.", "icon-copy", true)
        ), "Cartão");

        var htmls = List.of(
                template.lembrete(d), template.agendamentoConfirmado(d), template.pagamentoAprovado(d),
                template.reagendamento(d), template.cancelamento(d), template.reembolsoIniciado(refund),
                template.reembolsoConcluido(refund)
        );

        assertEquals(7, htmls.size());
        for (var html : htmls) {
            assertTrue(html.contains("cid:julyana-email-header"));
            assertTrue(html.contains("cid:footer-background"));
            assertFalse(html.contains("%s"));
            assertFalse(html.contains("%d"));
        }
    }

    @Test
    void deveEscaparDadosInseridosNoHtml() {
        var html = template.recuperarSenha("<teste>", "12&34");
        assertTrue(html.contains("12&amp;34"));
        assertFalse(html.contains("<teste>"));
    }
}
