package br.com.agendajulyana.auth.service;

public interface EmailService {
    void enviarCodigoRecuperacao(String email, String nome, String codigo);
}
