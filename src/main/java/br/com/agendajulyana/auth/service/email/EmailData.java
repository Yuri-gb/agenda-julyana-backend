package br.com.agendajulyana.auth.service.email;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class EmailData {
    private EmailData() {}

    public enum PaymentType { TOTAL, ENTRADA }

    public record EmailAppointmentData(
            String nomeCliente, String nomeServico, String descricaoServico,
            LocalDate data, LocalTime horario, int duracaoMinutos, String codigoAgendamento,
            BigDecimal valorServico, BigDecimal valorPago, BigDecimal valorRestante,
            PaymentType tipoPagamento) {}

    public record EmailInfoItem(String asset, String texto) {}

    public record EmailStatusStep(String titulo, String descricao, String asset, boolean concluida) {}

    public record EmailFooterData(String instagramUrl, String whatsappUrl, String localizacaoUrl) {
        public static EmailFooterData padrao() {
            return new EmailFooterData("#", "#", "#");
        }
    }

    public record EmailRefundData(
            EmailAppointmentData atendimento,
            BigDecimal valorReembolso,
            LocalDateTime solicitadoEm,
            List<EmailStatusStep> etapas,
            String formaEstorno) {}
}
