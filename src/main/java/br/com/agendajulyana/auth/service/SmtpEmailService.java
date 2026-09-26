package br.com.agendajulyana.auth.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplate template;

    public SmtpEmailService(JavaMailSender mailSender, EmailTemplate template) {
        this.mailSender = mailSender;
        this.template = template;
    }

    @Override
    public void enviarCodigoRecuperacao(String email, String nome, String codigo) {
        try {
            var mensagem = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Agenda Julyana — recuperação de senha");
            helper.setText(
                    """
                    Olá, %s!

                    Seu código para redefinir a senha da Agenda Julyana é: %s

                    Este código expira em 15 minutos e pode ser usado uma única vez.

                    Se você não solicitou a recuperação de senha, ignore este e-mail.
                    """.formatted(nome, codigo),
                    template.recuperarSenha(nome, codigo)
            );

            helper.addInline(
                    "julyana-email-header",
                    new ClassPathResource("email/julyana-email-header.jpg"),
                    "image/jpeg"
            );

            mailSender.send(mensagem);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível enviar o e-mail de recuperação.", e);
        }
    }
}
