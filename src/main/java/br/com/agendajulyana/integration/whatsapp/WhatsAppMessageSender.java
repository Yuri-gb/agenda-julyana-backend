package br.com.agendajulyana.integration.whatsapp;

public interface WhatsAppMessageSender {
    void sendVerificationCode(String telefone, String codigo);
}
