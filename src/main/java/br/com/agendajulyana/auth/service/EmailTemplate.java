package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.service.email.AgendaEmailRenderer;
import br.com.agendajulyana.auth.service.email.EmailData.EmailAppointmentData;
import br.com.agendajulyana.auth.service.email.EmailData.EmailRefundData;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {
    private final AgendaEmailRenderer renderer = new AgendaEmailRenderer();

    public String recuperarSenha(String nome, String codigo) {
        return renderer.recuperarSenha(nome, codigo);
    }

    public String lembrete(EmailAppointmentData data) {
        return renderer.lembrete(data);
    }

    public String agendamentoConfirmado(EmailAppointmentData data) {
        return renderer.agendamentoConfirmado(data);
    }

    public String pagamentoAprovado(EmailAppointmentData data) {
        return renderer.pagamentoAprovado(data);
    }

    public String reagendamento(EmailAppointmentData data) {
        return renderer.reagendamento(data);
    }

    public String cancelamento(EmailAppointmentData data) {
        return renderer.cancelamento(data);
    }

    public String reembolsoIniciado(EmailRefundData data) {
        return renderer.reembolsoIniciado(data);
    }

    public String reembolsoConcluido(EmailRefundData data) {
        return renderer.reembolsoConcluido(data);
    }
}
