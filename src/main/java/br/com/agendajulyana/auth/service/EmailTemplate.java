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
                <h1 style="margin:0 0 10px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:42px;line-height:1.15;font-weight:400;">
                    Olá, %s!
                </h1>

                <p style="margin:0 0 24px;color:#263022;font-family:Georgia,'Times New Roman',serif;font-size:19px;line-height:1.55;">
                    Recebemos uma solicitação para redefinir a senha da sua conta na Agenda Julyana.
                </p>

                <p style="margin:0 0 12px;color:%s;font-family:Arial,sans-serif;font-size:17px;line-height:1.5;">
                    Use o código abaixo para continuar:
                </p>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 28px;">
                  <tr>
                    <td style="padding:20px 22px;border:2px solid %s;border-radius:18px;background:%s;">
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                          <td align="center" valign="middle">
                            <span style="color:%s;font-family:Arial,sans-serif;font-size:38px;line-height:1;font-weight:700;letter-spacing:9px;">
                                %s
                            </span>
                          </td>
                          <td width="82" align="center" valign="middle" style="border-left:1px solid #d8c48e;padding-left:12px;">
                            <div style="font-family:Arial,sans-serif;color:%s;font-size:25px;line-height:1;margin-bottom:5px;">⧉</div>
                            <div style="font-family:Arial,sans-serif;color:%s;font-size:11px;line-height:1.2;letter-spacing:.5px;">
                                COPIAR<br>CÓDIGO
                            </div>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 30px;">
                  <tr>
                    <td width="50%%" valign="top" style="padding:0 8px 0 0;">
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                          <td width="58" valign="middle">
                            <div style="width:48px;height:48px;border-radius:50%%;background:#eef0dc;text-align:center;font-family:Arial,sans-serif;color:%s;font-size:25px;line-height:48px;">◷</div>
                          </td>
                          <td valign="middle" style="padding-left:8px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:16px;line-height:1.4;">
                            Este código é válido por<br><strong>15 minutos.</strong>
                          </td>
                        </tr>
                      </table>
                    </td>
                    <td width="50%%" valign="top" style="padding:0 0 0 8px;border-left:1px solid #d8d0b8;">
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                          <td width="58" valign="middle">
                            <div style="width:48px;height:48px;border-radius:50%%;background:#eef0dc;text-align:center;font-family:Arial,sans-serif;color:%s;font-size:23px;line-height:48px;">♢</div>
                          </td>
                          <td valign="middle" style="padding-left:8px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:16px;line-height:1.4;">
                            Pode ser utilizado<br>apenas <strong>uma única vez.</strong>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 34px;">
                  <tr>
                    <td style="padding:20px 22px;background:#ebe9d9;border-radius:15px;">
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                          <td width="66" valign="top" align="center">
                            <div style="font-family:Arial,sans-serif;color:%s;font-size:37px;line-height:1.1;">♙</div>
                          </td>
                          <td valign="top">
                            <p style="margin:0 0 7px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:18px;line-height:1.4;font-weight:700;">
                                Não solicitou a recuperação de senha?
                            </p>
                            <p style="margin:0;color:#35412e;font-family:Arial,sans-serif;font-size:15px;line-height:1.55;">
                                Se você não fez essa solicitação, ignore este e-mail. Sua conta continuará segura.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 20px;">
                  <tr>
                    <td width="42%%" style="border-top:1px solid #cdbb8c;"></td>
                    <td width="16%%" align="center" style="color:%s;font-size:26px;line-height:1;">♧</td>
                    <td width="42%%" style="border-top:1px solid #cdbb8c;"></td>
                  </tr>
                </table>

                <div style="text-align:center;">
                    <p style="margin:0 0 6px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:20px;line-height:1.4;">
                        Cuidar de você é a nossa essência.
                    </p>
                    <p style="margin:0;color:%s;font-family:Arial,sans-serif;font-size:14px;line-height:1.4;">
                        Julyana Lima — Estética e Bem-estar
                    </p>
                </div>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-top:30px;">
                  <tr>
                    <td width="25%%" align="center" valign="top">
                      <div style="width:44px;height:44px;margin:0 auto 7px;border-radius:50%%;background:#eef0dc;color:%s;font-family:Arial,sans-serif;font-size:23px;line-height:44px;">♧</div>
                      <div style="color:%s;font-family:Arial,sans-serif;font-size:12px;line-height:1.35;">Estética<br>Facial e Corporal</div>
                    </td>
                    <td width="25%%" align="center" valign="top">
                      <div style="width:44px;height:44px;margin:0 auto 7px;border-radius:50%%;background:#eef0dc;color:%s;font-family:Arial,sans-serif;font-size:22px;line-height:44px;">⌁</div>
                      <div style="color:%s;font-family:Arial,sans-serif;font-size:12px;line-height:1.35;">Massoterapia</div>
                    </td>
                    <td width="25%%" align="center" valign="top">
                      <div style="width:44px;height:44px;margin:0 auto 7px;border-radius:50%%;background:#eef0dc;color:%s;font-family:Arial,sans-serif;font-size:22px;line-height:44px;">♨</div>
                      <div style="color:%s;font-family:Arial,sans-serif;font-size:12px;line-height:1.35;">Relaxamento</div>
                    </td>
                    <td width="25%%" align="center" valign="top">
                      <div style="width:44px;height:44px;margin:0 auto 7px;border-radius:50%%;background:#eef0dc;color:%s;font-family:Arial,sans-serif;font-size:22px;line-height:44px;">♡</div>
                      <div style="color:%s;font-family:Arial,sans-serif;font-size:12px;line-height:1.35;">Bem-estar</div>
                    </td>
                  </tr>
                </table>
                """.formatted(
                    VERDE_ESCURO, escape(nome), VERDE_OLIVA, DOURADO, CREME, DOURADO, escape(codigo),
                    DOURADO, VERDE_OLIVA,
                    VERDE_ESCURO, VERDE_ESCURO,
                    VERDE_ESCURO, VERDE_OLIVA,
                    DOURADO, VERDE_ESCURO, VERDE_ESCURO,
                    DOURADO, VERDE_OLIVA,
                    VERDE_OLIVA, VERDE_OLIVA,
                    VERDE_OLIVA, VERDE_OLIVA,
                    VERDE_OLIVA, VERDE_OLIVA
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
                        <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0" style="width:100%%;max-width:600px;background:#fffdf8;border-radius:18px;overflow:hidden;">
                          <tr>
                            <td style="padding:0;">
                              <img src="cid:julyana-email-header" width="600" alt="Julyana Lima — estética e bem-estar" style="display:block;width:100%%;max-width:600px;height:auto;border:0;">
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:40px 36px 38px;">
                              %s
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px 36px 26px;background:%s;text-align:center;border-radius:38px 38px 0 0;">
                              <p style="margin:0 0 9px;color:#f8f1e5;font-family:Georgia,'Times New Roman',serif;font-size:17px;line-height:1.4;">
                                Siga nossas redes
                              </p>
                              <p style="margin:0 0 14px;color:#f8f1e5;font-family:Arial,sans-serif;font-size:25px;line-height:1;">
                                ◎ &nbsp;&nbsp;◉ &nbsp;&nbsp;⌖
                              </p>
                              <p style="margin:0;color:#f8f1e5;font-family:Arial,sans-serif;font-size:14px;line-height:1.5;">
                                <strong>⌖ &nbsp; Feira de Santana - BA</strong><br>
                                <span style="font-size:12px;">Beleza, saúde e bem-estar em um só lugar.</span>
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
