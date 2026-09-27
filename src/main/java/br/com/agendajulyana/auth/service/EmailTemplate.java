package br.com.agendajulyana.auth.service;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {

    private static final String VERDE_ESCURO = "#20351f";
    private static final String VERDE_FUNDO = "#46522f";
    private static final String VERDE_ICON = "#5c6b35";
    private static final String FUNDO_ICONE = "#eeeddf";
    private static final String DOURADO = "#b38a32";
    private static final String CREME = "#f7f1e5";
    private static final String CREME_CARD = "#efeedf";
    private static final String FUNDO = "#eee8dc";

    public String recuperarSenha(String nome, String codigo) {
        var content = """
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                  <tr>
                    <td style="padding:0;">

                      <h1 style="margin:0 0 12px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:48px;line-height:1.1;font-weight:400;">
                        Olá, %s!
                      </h1>

                      <p style="margin:0 0 25px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:20px;line-height:1.55;">
                        Recebemos uma solicitação para redefinir<br class="desktop-only">
                        a senha da sua conta na Agenda Julyana.
                      </p>

                      <p style="margin:0 0 12px;color:#697153;font-family:Arial,sans-serif;font-size:18px;line-height:1.5;">
                        Use o código abaixo para continuar:
                      </p>

                      <!-- Código + ação visual de copiar -->
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 24px;">
                        <tr>
                          <td style="padding:24px 24px;border:2px solid %s;border-radius:18px;background:%s;min-height:76px;">
                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                              <tr>
                                <td align="center" valign="middle">
                                  <span style="color:%s;font-family:Arial,sans-serif;font-size:42px;line-height:1.05;font-weight:700;letter-spacing:8px;">
                                    %s
                                  </span>
                                </td>
                                <td width="112" align="center" valign="middle" style="border-left:1px solid #d7c796;padding-left:12px;">
                                  <!-- Ícone SVG preparado para a ação de copiar -->
                                  <svg width="28" height="28" viewBox="0 0 28 28" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" style="display:block;margin:0 auto 5px;">
                                    <rect x="8" y="4" width="14" height="17" rx="2" fill="none" stroke="%s" stroke-width="1.8"/>
                                    <rect x="4" y="8" width="14" height="17" rx="2" fill="%s" stroke="%s" stroke-width="1.8"/>
                                  </svg>
                                  <div style="font-family:Arial,sans-serif;color:%s;font-size:10px;line-height:1.25;letter-spacing:.4px;">
                                    COPIAR<br>CÓDIGO
                                  </div>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>

                      <!-- Informações: ícone à esquerda + texto alinhado à esquerda -->
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 24px;">
                        <tr>
                          <td class="info-column" width="50%%" valign="middle" style="padding:0 18px 0 0;">
                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                              <tr>
                                <td width="58" valign="middle" style="padding:0;">
                                  <div style="width:48px;height:48px;margin:0;border-radius:50%%;background:%s;">
                                    <!-- ASSET: icon-validade -->
                                  </div>
                                </td>
                                <td valign="middle" align="left" style="padding-left:12px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:18px;line-height:1.45;text-align:left;">
                                  Este código é válido por<br>
                                  <strong style="color:#20351f;">15 minutos.</strong>
                                </td>
                              </tr>
                            </table>
                          </td>

                          <td class="info-column" width="50%%" valign="middle" style="padding:0 0 0 18px;border-left:1px solid #d9d0b7;">
                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                              <tr>
                                <td width="58" valign="middle" style="padding:0;">
                                  <div style="width:48px;height:48px;margin:0;border-radius:50%%;background:%s;">
                                    <!-- ASSET: icon-uso-unico -->
                                  </div>
                                </td>
                                <td valign="middle" align="left" style="padding-left:12px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:18px;line-height:1.45;text-align:left;">
                                  Pode ser utilizado<br>
                                  apenas <strong style="color:#20351f;">uma única vez.</strong>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>

                      <!-- Aviso de segurança: um único bloco, sem badge -->
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 24px;">
                        <tr>
                          <td style="padding:28px 30px;background:%s;border-radius:16px;min-height:96px;">
                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                              <tr>
                                <td width="76" valign="middle" align="center">
                                  <div style="width:58px;height:58px;border-radius:50%%;background:#eeeddf;">
                                    <!-- ASSET: icon-seguranca -->
                                  </div>
                                </td>
                                <td valign="middle" style="padding-left:12px;">
                                  <p style="margin:0 0 5px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:19px;line-height:1.35;font-weight:700;">
                                    Não solicitou a recuperação de senha?
                                  </p>
                                  <p style="margin:0;color:#35412e;font-family:Arial,sans-serif;font-size:15px;line-height:1.5;">
                                    Se você não fez essa solicitação, ignore este e-mail.<br class="desktop-only">
                                    Sua conta continuará segura.
                                  </p>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>

                      <!-- Assinatura -->
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 24px;">
                        <tr>
                          <td width="39%%" valign="middle" style="border-top:1px solid #cdbb8c;"></td>
                          <td width="22%%" align="center" valign="middle" style="padding:0 10px;">
                            <div style="width:38px;height:38px;margin:0 auto;border-radius:50%%;background:%s;line-height:38px;text-align:center;">
                              <!-- ASSET: icon-assinatura -->
                            </div>
                          </td>
                          <td width="39%%" valign="middle" style="border-top:1px solid #cdbb8c;"></td>
                        </tr>
                      </table>

                      <div style="text-align:center;">
                        <p style="margin:0 0 5px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:22px;line-height:1.4;">
                          Cuidar de você é a nossa essência.
                        </p>
                        <p style="margin:0;color:#687151;font-family:Arial,sans-serif;font-size:15px;line-height:1.4;">
                          Julyana Lima — Estética e Bem-estar
                        </p>
                      </div>

                      <!-- Serviços -->
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-top:26px;">
                        <tr>
                          <td class="service-column" width="25%%" align="center" valign="top" style="padding:0 4px;">
                            <div style="width:60px;height:60px;margin:0 auto 9px;border-radius:50%%;background:%s;">
                              <!-- ASSET: icon-estetica -->
                            </div>
                            <div style="color:#687151;font-family:Arial,sans-serif;font-size:14px;line-height:1.35;">
                              Estética<br>Facial e Corporal
                            </div>
                          </td>
                          <td class="service-column" width="25%%" align="center" valign="top" style="padding:0 4px;">
                            <div style="width:60px;height:60px;margin:0 auto 9px;border-radius:50%%;background:%s;">
                              <!-- ASSET: icon-massoterapia -->
                            </div>
                            <div style="color:#687151;font-family:Arial,sans-serif;font-size:14px;line-height:1.35;">
                              Massoterapia
                            </div>
                          </td>
                          <td class="service-column" width="25%%" align="center" valign="top" style="padding:0 4px;">
                            <div style="width:60px;height:60px;margin:0 auto 9px;border-radius:50%%;background:%s;">
                              <!-- ASSET: icon-relaxamento -->
                            </div>
                            <div style="color:#687151;font-family:Arial,sans-serif;font-size:14px;line-height:1.35;">
                              Relaxamento
                            </div>
                          </td>
                          <td class="service-column" width="25%%" align="center" valign="top" style="padding:0 4px;">
                            <div style="width:60px;height:60px;margin:0 auto 9px;border-radius:50%%;background:%s;">
                              <!-- ASSET: icon-bem-estar -->
                            </div>
                            <div style="color:#687151;font-family:Arial,sans-serif;font-size:14px;line-height:1.35;">
                              Bem-estar
                            </div>
                          </td>
                        </tr>
                      </table>

                    </td>
                  </tr>
                </table>
                """.formatted(
                VERDE_ESCURO,
                escape(nome),
                VERDE_ESCURO,
                DOURADO,
                CREME,
                VERDE_ESCURO,
                escape(codigo),
                DOURADO,
                CREME,
                DOURADO,
                VERDE_ICON,
                VERDE_ICON,
                VERDE_ICON,
                FUNDO_ICONE,
                VERDE_ESCURO,
                CREME_CARD,
                VERDE_ESCURO,
                FUNDO_ICONE,
                VERDE_ESCURO,
                FUNDO_ICONE,
                FUNDO_ICONE,
                FUNDO_ICONE,
                FUNDO_ICONE,
                VERDE_ICON
        );

        return base(content);
    }

    private String base(String content) {
        return """
                <!doctype html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <meta name="x-apple-disable-message-reformatting">
                  <meta name="color-scheme" content="light dark">
                  <meta name="supported-color-schemes" content="light dark">
                  <title>Agenda Julyana</title>
                  <style>
                    @media only screen and (max-width: 480px) {
                      .email-shell {
                        width:100%% !important;
                        max-width:100%% !important;
                        border-radius:0 !important;
                      }
                      .email-content {
                        padding:30px 22px 28px !important;
                      }
                      .desktop-only {
                        display:none !important;
                      }
                      .info-column {
                        display:block !important;
                        width:100%% !important;
                        padding:0 0 22px !important;
                        border-left:0 !important;
                      }
                      .service-column {
                        width:50%% !important;
                        padding-bottom:22px !important;
                      }
                      .footer-column {
                        display:block !important;
                        width:100%% !important;
                        box-sizing:border-box !important;
                        border-right:0 !important;
                        border-bottom:1px solid rgba(247,241,229,.55) !important;
                      }
                    }
                  </style>
                </head>
                <body style="margin:0;padding:0;background:%s;">
                  <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;">
                    Comunicação da Agenda Julyana.
                  </div>

                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="background:%s;">
                    <tr>
                      <td align="center" style="padding:24px 12px;">
                        <table class="email-shell" role="presentation" width="600" cellpadding="0" cellspacing="0" border="0" style="width:100%%;max-width:700px;background:%s;border-radius:18px;overflow:hidden;">

                          <!-- Header -->
                          <tr>
                            <td style="padding:0;">
                              <img src="cid:julyana-email-header" width="700" alt="Julyana Lima — estética e bem-estar" style="display:block;width:100%%;max-width:700px;height:auto;border:0;">
                            </td>
                          </tr>

                          <!-- Conteúdo -->
                          <tr>
                            <td class="email-content" style="padding:52px 64px 48px;">
                              %s
                            </td>
                          </tr>

                          <!-- Footer full-bleed com topo orgânico -->
                          <tr>
                            <td width="100%%" style="padding:0;background:%s;border-radius:0 0 18px 18px;overflow:hidden;">
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                  <td style="padding:0;background:%s;height:34px;line-height:0;font-size:0;">
                                    <svg width="100%%" height="34" viewBox="0 0 700 34" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg" style="display:block;width:100%%;height:34px;">
                                      <path d="M0,0 C145,22 220,31 350,30 C480,29 555,20 700,0 L700,34 L0,34 Z" fill="#46522f"/>
                                    </svg>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:0;background:%s;">
                                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                                      <tr>
                                        <td class="footer-column" width="50%%" valign="middle" style="padding:10px 22px 28px 34px;border-right:1px solid rgba(247,241,229,.55);">
                                          <p style="margin:0 0 11px;color:%s;font-family:Georgia,'Times New Roman',serif;font-size:16px;line-height:1.35;">
                                            Siga nossas redes
                                          </p>
                                          <table role="presentation" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                              <td style="padding-right:10px;">
                                                <div style="width:34px;height:34px;border:1px solid %s;border-radius:50%%;text-align:center;">
                                                  <!-- ASSET: icon-instagram -->
                                                </div>
                                              </td>
                                              <td style="padding-right:10px;">
                                                <div style="width:34px;height:34px;border:1px solid %s;border-radius:50%%;text-align:center;">
                                                  <!-- ASSET: icon-whatsapp -->
                                                </div>
                                              </td>
                                              <td>
                                                <div style="width:34px;height:34px;border:1px solid %s;border-radius:50%%;text-align:center;">
                                                  <!-- ASSET: icon-localizacao -->
                                                </div>
                                              </td>
                                            </tr>
                                          </table>
                                        </td>

                                        <td class="footer-column" width="50%%" valign="middle" style="padding:10px 34px 28px 22px;">
                                          <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                              <td width="42" valign="middle" style="padding-right:10px;">
                                                <div style="width:34px;height:34px;border-radius:50%%;border:1px solid %s;text-align:center;">
                                                  <!-- ASSET: icon-localizacao-footer -->
                                                </div>
                                              </td>
                                              <td valign="middle">
                                                <p style="margin:0 0 3px;color:%s;font-family:Arial,sans-serif;font-size:14px;line-height:1.35;font-weight:700;">
                                                  Feira de Santana - BA
                                                </p>
                                                <p style="margin:0;color:%s;font-family:Arial,sans-serif;font-size:11px;line-height:1.4;">
                                                  Beleza, saúde e bem-estar em um só lugar.
                                                </p>
                                              </td>
                                            </tr>
                                          </table>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                FUNDO,
                FUNDO,
                CREME,
                content,
                VERDE_FUNDO,
                VERDE_FUNDO,
                VERDE_FUNDO,
                CREME,
                DOURADO,
                DOURADO,
                DOURADO,
                DOURADO,
                CREME,
                CREME,
                CREME
        );
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
