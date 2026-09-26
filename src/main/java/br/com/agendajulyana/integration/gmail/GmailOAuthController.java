package br.com.agendajulyana.integration.gmail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integrations/google/gmail")
@ConditionalOnProperty(name = "app.gmail.authorization.enabled", havingValue = "true")
public class GmailOAuthController {

    private final GmailOAuthService oauthService;

    public GmailOAuthController(GmailOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/authorize")
    public String authorize() {
        return """
                <!doctype html>
                <html lang="pt-BR">
                <meta charset="UTF-8">
                <title>Autorizar Gmail</title>
                <body style="font-family:Arial,sans-serif;padding:40px">
                  <h1>Autorizar envio de e-mails</h1>
                  <p>Você será redirecionado para o Google para autorizar o Agenda Julyana a enviar e-mails pela conta remetente.</p>
                  <a href="%s">Continuar com Google</a>
                </body>
                </html>
                """.formatted(oauthService.criarUrlAutorizacao());
    }

    @GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
    public String callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        if (error != null) {
            return pagina("Autorização cancelada", "O Google não autorizou o acesso: " + escape(error));
        }

        if (code == null || state == null) {
            return pagina("Autorização inválida", "O Google não retornou os parâmetros esperados.");
        }

        var refreshToken = oauthService.trocarCodigoPorRefreshToken(code, state);

        return pagina(
                "Autorização concluída",
                "<p>O Google autorizou o envio.</p>"
                        + "<p><strong>Salve o refresh token abaixo como segredo no Render.</strong></p>"
                        + "<textarea style=\"width:100%;height:180px\">"
                        + escape(refreshToken)
                        + "</textarea>"
                        + "<p>Depois disso, desative <code>app.gmail.authorization.enabled</code>.</p>"
        );
    }

    private static String pagina(String titulo, String corpo) {
        return """
                <!doctype html>
                <html lang="pt-BR">
                <meta charset="UTF-8">
                <title>%s</title>
                <body style="font-family:Arial,sans-serif;max-width:760px;margin:40px auto;padding:0 20px">
                  <h1>%s</h1>
                  %s
                </body>
                </html>
                """.formatted(titulo, titulo, corpo);
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
