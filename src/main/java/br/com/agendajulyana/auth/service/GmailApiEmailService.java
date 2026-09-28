package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.integration.gmail.GmailApiService;
import org.springframework.stereotype.Service;

@Service
public class GmailApiEmailService implements EmailService {

    private final GmailApiService gmailApiService;
    private final EmailTemplate template;

    public GmailApiEmailService(GmailApiService gmailApiService, EmailTemplate template) {
        this.gmailApiService = gmailApiService;
        this.template = template;
    }

    @Override
    public void enviarCodigoRecuperacao(String email, String nome, String codigo) {
        var texto = """
                Olá, %s!

                Seu código para redefinir a senha da Agenda Julyana é: %s

                Este código expira em 15 minutos e pode ser usado uma única vez.

                Se você não solicitou a recuperação de senha, ignore este e-mail.
                """.formatted(nome, codigo);

        gmailApiService.enviar(
                email,
                nome,
                "Agenda Julyana — recuperação de senha",
                template.recuperarSenha(nome, codigo),
                texto
        );
    }
}
