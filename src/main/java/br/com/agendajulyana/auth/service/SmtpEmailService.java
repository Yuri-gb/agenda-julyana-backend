package br.com.agendajulyana.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {
    private final JavaMailSender mailSender;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarCodigoRecuperacao(String email, String nome, String codigo) {
        var mensagem = new SimpleMailMessage();
        mensagem.setTo(email);
        mensagem.setSubject("Agenda Julyana — recuperação de senha");
        mensagem.setText("""
            Olá, %s!

            Seu código para redefinir a senha da Agenda Julyana é:

            %s

            Este código expira em 15 minutos e pode ser usado uma única vez.

            Se você não solicitou a recuperação de senha, ignore este e-mail.
            """.formatted(nome, codigo));
        mailSender.send(mensagem);
    }
}
