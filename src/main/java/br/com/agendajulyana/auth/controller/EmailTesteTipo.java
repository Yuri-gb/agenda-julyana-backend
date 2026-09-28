package br.com.agendajulyana.auth.controller;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipos de e-mail disponíveis para teste.")
public enum EmailTesteTipo {
    RECUPERACAO_SENHA,
    LEMBRETE,
    AGENDAMENTO_CONFIRMADO,
    PAGAMENTO_APROVADO,
    REAGENDAMENTO,
    CANCELAMENTO,
    REEMBOLSO_INICIADO,
    REEMBOLSO_CONCLUIDO
}
