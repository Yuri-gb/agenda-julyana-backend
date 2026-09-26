package br.com.agendajulyana.auth.service;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {

    private static final String VERDE_ESCURO = "#20351f";
    private static final String VERDE_OLIVA = "#58633b";
    private static final String DOURADO = "#b38a32";
    private static final String CREME = "#f7f1e5";
    private static final String FUNDO = "#eee8dc";

    public String recuperarSenha(String nome, String codigo) {
        return base("""
                <h1 style="margin:0 0 8px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:38px;line-height:1.15;font-weight:400;">
                    Olá, %s!
                </h1>
                <p style="margin:0 0 22px;color:#263022;font-family:Georgia,'Times New Roman',serif;font-size:18px;line-height:1.55;">
                    Recebemos uma solicitação para redefinir a senha da sua conta na Agenda Julyana.
                </p>
                <p style="margin:0 0 12px;color:%s;font-family:Arial,sans-serif;font-size:16px;line-height:1.5;">
                    Use o código abaixo para continuar:
                </p>
                <div style="margin:0 0 26px;padding:22px 24px;border:2px solid %s;border-radius:16px;background:%s;text-align:center;">
                    <span style="color:%s;font-family:Arial,sans-serif;font-size:36px;line-height:1;font-weight:700;letter-spacing:8px;">
                        %s
                    </span>
                </div>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 28px;">
                    <tr>
                        <td width="50%%" valign="top" style="padding:0 8px 0 0;">
                            <div style="padding:16px;background:#f1f0df;border-radius:14px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:15px;line-height:1.45;">
                                Este código é válido por <strong>15 minutos.</strong>
                            </div>
                        </td>
                        <td width="50%%" valign="top" style="padding:0 0 0 8px;">
                            <div style="padding:16px;background:#f1f0df;border-radius:14px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:15px;line-height:1.45;">
                                Pode ser utilizado apenas <strong>uma única vez.</strong>
                            </div>
                        </td>
                    </tr>
                </table>
                <div style="margin:0 0 30px;padding:20px 22px;background:#ebe9d9;border-radius:14px;">
                    <p style="margin:0 0 8px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:17px;line-height:1.4;font-weight:700;">
                        Não solicitou a recuperação de senha?
                    </p>
                    <p style="margin:0;color:#35412e;font-family:Arial,sans-serif;font-size:15px;line-height:1.55;">
                        Se você não fez essa solicitação, ignore este e-mail. Sua conta continuará segura.
                    </p>
                </div>
                <div style="border-top:1px solid #cdbb8c;padding-top:22px;text-align:center;">
                    <p style="margin:0 0 6px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:18px;line-height:1.4;">
                        Cuidar de você é a nossa essência.
                    </p>
                    <p style="margin:0;color:%s;font-family:Arial,sans-serif;font-size:14px;line-height:1.4;">
                        Julyana Lima — Estética e Bem-estar
                    </p>
                </div>
                """.formatted(
                    VERDE_ESCURO, escape(nome), VERDE_OLIVA, DOURADO, CREME, DOURADO, escape(codigo),
                    VERDE_ESCURO, VERDE_ESCURO, VERDE_OLIVA, DOURADO, VERDE_ESCURO
                ));
    }

    private String base(String content) {
        return """
                <!doctype html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <meta name="x-apple-disable-message-reformatting">
                  <title>Agenda Julyana</title>
                </head>
                <body style="margin:0;padding:0;background:%s;">
                  <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;">
                    Comunicação da Agenda Julyana.
                  </div>
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="background:%s;">
                    <tr>
                      <td align="center" style="padding:24px 12px;">
                        <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0" style="width:100%%;max-width:600px;background:#fffdf8;border-radius:16px;overflow:hidden;">
                          <tr>
                            <td style="padding:0;">
                              <img src="cid:julyana-email-header" width="600" alt="Julyana Lima — estética e bem-estar" style="display:block;width:100%%;max-width:600px;height:auto;border:0;">
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:40px 36px 34px;">
                              %s
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 36px 22px;background:%s;text-align:center;">
                              <p style="margin:0;color:#f8f1e5;font-family:Arial,sans-serif;font-size:12px;line-height:1.5;">
                                Julyana Lima — Estética e Bem-estar
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(FUNDO, FUNDO, content, VERDE_ESCURO);
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
