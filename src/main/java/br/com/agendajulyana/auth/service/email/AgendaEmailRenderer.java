package br.com.agendajulyana.auth.service.email;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

import br.com.agendajulyana.auth.service.email.EmailData.EmailAppointmentData;
import br.com.agendajulyana.auth.service.email.EmailData.EmailInfoItem;
import br.com.agendajulyana.auth.service.email.EmailData.EmailRefundData;
import br.com.agendajulyana.auth.service.email.EmailData.PaymentType;

public final class AgendaEmailRenderer {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final String VERDE = "#20351f";
    private static final String VERDE_FUNDO = "#17351c";
    private static final String CREME = "#f7f1e5";
    private static final String CARD = "#efeedf";
    private static final String ICONE = "#eeeddf";
    private static final String DOURADO = "#b38a32";

    private final DateTimeFormatter data = DateTimeFormatter.ofPattern("dd 'de' MMM 'de' yyyy", PT_BR);
    private final DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm");

    public String recuperarSenha(String nome, String codigo) {
        return layout(hero("icon-lock", "Recuperação de senha", nome,
                "Recebemos uma solicitação para redefinir a senha da sua conta na Agenda Julyana.")
                + """
                <p style="margin:0 0 12px;color:#697153;font:18px/1.5 Arial,sans-serif;">Use o código abaixo para continuar:</p>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 24px;"><tr><td style="padding:24px;border:2px solid %s;border-radius:18px;background:%s;"><table role="presentation" width="100%%"><tr><td align="center"><strong style="color:%s;font:700 42px/1.05 Arial,sans-serif;letter-spacing:8px;">%s</strong></td><td width="112" align="center" style="border-left:1px solid #d7c796;padding-left:12px;">%s<div style="font:10px/1.25 Arial,sans-serif;color:%s;">COPIAR<br>CÓDIGO</div></td></tr></table></td></tr></table>
                """.formatted(DOURADO, CREME, VERDE, esc(codigo), icon("icon-copy", 28), VERDE)
                + twoInfo("icon-horario", "Este código é válido por", "15 minutos.", "icon-copy", "Pode ser utilizado", "apenas uma única vez.")
                + block("icon-seguranca", "Não solicitou a recuperação de senha?", "Se você não fez essa solicitação, ignore este e-mail. Sua conta continuará segura.")
                + closing());
    }

    public String lembrete(EmailAppointmentData d) {
        return layout(hero("icon-horario", "Seu atendimento está chegando!", d.nomeCliente(),
                "Passando para lembrar do seu atendimento. Estamos te esperando para proporcionar esse momento de bem-estar!")
                + appointment(d, "Seu atendimento")
                + info("Informações importantes", "icon-seguranca", new EmailInfoItem("icon-horario", "Chegue com 10 minutos de antecedência."), new EmailInfoItem("icon-calendar", "Em caso de imprevistos, você pode reagendar até 2 vezes."), new EmailInfoItem("icon-whatsapp", "Em caso de dúvidas, entre em contato conosco."))
                + cta("VER ENDEREÇO DO ATENDIMENTO", d.codigoAgendamento(), "icon-localizacao") + closing());
    }

    public String agendamentoConfirmado(EmailAppointmentData d) {
        return layout(hero("icon-confirmado", "Agendamento confirmado!", d.nomeCliente(),
                "Seu atendimento foi confirmado com sucesso. Abaixo estão os detalhes do seu agendamento.")
                + appointment(d, "Seu atendimento") + payment(d)
                + block("icon-seguranca", "Está tudo certo!", "Seu horário está reservado e seu agendamento foi confirmado. Guarde este e-mail para consultar os detalhes do seu atendimento.")
                + cta("VER MEU AGENDAMENTO", d.codigoAgendamento(), "icon-calendar") + closing());
    }

    public String pagamentoAprovado(EmailAppointmentData d) {
        var descricao = d.tipoPagamento() == PaymentType.ENTRADA
                ? "Recebemos e aprovamos o pagamento da sua entrada. Seu agendamento está confirmado e o valor restante será pago no dia do atendimento."
                : "Recebemos e aprovamos o pagamento do seu atendimento. Seu agendamento está confirmado.";
        return layout(hero("icon-pagamento", "Pagamento aprovado!", d.nomeCliente(), descricao)
                + appointment(d, "Seu atendimento") + payment(d)
                + info("Informações importantes", "icon-seguranca", new EmailInfoItem("icon-confirmado", "Seu agendamento continua confirmado."), new EmailInfoItem("icon-copy", d.tipoPagamento() == PaymentType.ENTRADA ? "O valor restante será pago no dia do atendimento." : "O pagamento foi aprovado com sucesso."), new EmailInfoItem("icon-whatsapp", "Em caso de dúvidas, entre em contato conosco."))
                + cta("VER MEU AGENDAMENTO", d.codigoAgendamento(), "icon-calendar") + closing());
    }

    public String reagendamento(EmailAppointmentData d) {
        return layout(hero("icon-reagendamento", "Seu atendimento foi reagendado!", d.nomeCliente(), "Seu atendimento foi reagendado com sucesso. Abaixo estão os novos detalhes do seu horário. Qualquer alteração, estamos à disposição!")
                + appointment(d, "Seu atendimento")
                + info("Informações importantes", "icon-seguranca", new EmailInfoItem("icon-horario", "Chegue com 10 minutos de antecedência."), new EmailInfoItem("icon-reagendamento", "Você pode reagendar mais uma vez."), new EmailInfoItem("icon-calendar", "Em caso de falta na sessão sem justificativa prévia ela é considerada como realizada."))
                + cta("VER MEU AGENDAMENTO", d.codigoAgendamento(), "icon-calendar") + closing());
    }

    public String cancelamento(EmailAppointmentData d) {
        return layout(hero("icon-cancelamento", "Seu atendimento foi cancelado", d.nomeCliente(), "Seu atendimento foi cancelado conforme solicitado. Se precisar, você pode agendar um novo horário quando quiser.")
                + appointment(d, "Atendimento cancelado")
                + info("O que fazer agora?", "icon-seguranca", new EmailInfoItem("icon-calendar", "Agende um novo horário quando quiser."), new EmailInfoItem("icon-whatsapp", "Em caso de dúvidas, entre em contato conosco."), new EmailInfoItem("icon-bem-estar", "Estamos à disposição para te atender novamente."))
                + cta("AGENDAR NOVO HORÁRIO", d.codigoAgendamento(), "icon-calendar") + closing());
    }

    public String reembolsoIniciado(EmailRefundData d) {
        var a = d.atendimento();
        return layout(hero("icon-reembolso-iniciado", "Seu reembolso foi iniciado!", a.nomeCliente(), "Seu cancelamento foi processado e o reembolso já foi solicitado na forma de pagamento utilizada. O prazo pode variar conforme a operadora.")
                + appointment(a, "Atendimento cancelado") + timeline(d)
                + refund("Forma de estorno", "O valor será estornado para o mesmo meio de pagamento utilizado na compra, conforme as regras da operadora.")
                + cta("ACOMPANHAR REEMBOLSO", a.codigoAgendamento(), "icon-copy") + closing());
    }

    public String reembolsoConcluido(EmailRefundData d) {
        var a = d.atendimento();
        return layout(hero("icon-reembolso-concluido", "Seu reembolso foi concluído!", a.nomeCliente(), "O reembolso do seu atendimento foi processado com sucesso e o valor já foi estornado para o mesmo meio de pagamento utilizado na compra.")
                + appointment(a, "Atendimento cancelado") + timeline(d)
                + refund("Forma de estorno", "O valor foi estornado para o mesmo meio de pagamento utilizado na compra, conforme as regras da operadora.")
                + cta("VER DETALHES DO REEMBOLSO", a.codigoAgendamento(), "icon-copy") + closing());
    }

    private String layout(String content) {
        return """
                <!doctype html><html lang="pt-BR"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="x-apple-disable-message-reformatting"><meta name="color-scheme" content="light dark"><style>@media only screen and (max-width:480px){.email-shell{width:100%% !important;border-radius:0!important}.email-content{padding:30px 20px!important}.stack{display:block!important;width:100%%!important;padding:0 0 18px!important;border:0!important}.hero-title{font-size:36px!important}.service{width:50%%!important;padding-bottom:20px!important}.footer-col{display:block!important;width:100%%!important;border:0!important;border-bottom:1px solid rgba(247,241,229,.5)!important}}</style></head><body style="margin:0;padding:0;background:#eee8dc;"><table role="presentation" width="100%%"><tr><td align="center" style="padding:24px 12px;"><table class="email-shell" role="presentation" width="700" style="width:100%%;max-width:700px;background:%s;border-radius:18px;overflow:hidden;"><tr><td><img src="cid:julyana-email-header" width="700" alt="Julyana Lima — estética e bem-estar" style="display:block;width:100%%;height:auto;border:0;"></td></tr><tr><td class="email-content" style="padding:48px 56px 42px;">%s</td></tr>%s</table></td></tr></table></body></html>
                """.formatted(CREME, content, footer());
    }

    private String hero(String asset, String title, String name, String description) {
        return """
                <table role="presentation" width="100%%" style="margin:0 0 24px;"><tr><td width="180" valign="top" align="center">%s</td><td valign="middle"><h1 class="hero-title" style="margin:0 0 8px;color:%s;font:700 46px/1.08 Georgia,serif;">%s</h1><p style="margin:0 0 5px;color:%s;font:700 22px/1.25 Georgia,serif;">Olá, %s!</p><p style="margin:0;color:%s;font:18px/1.4 Georgia,serif;">%s</p></td></tr></table>
                """.formatted(circle(asset, 150), VERDE, esc(title), VERDE, esc(name), VERDE, esc(description));
    }

    private String appointment(EmailAppointmentData d, String label) {
        var code = d.codigoAgendamento() == null || d.codigoAgendamento().isBlank() ? "" : "<td width=\"145\" style=\"padding-left:16px;border-left:1px solid #cdbb8c;\"><small>Código do agendamento</small><strong style=\"display:block;margin-top:8px;padding:8px;background:#efe3c7;border-radius:10px;text-align:center;color:" + VERDE + ";\">" + esc(d.codigoAgendamento()) + "</strong></td>";
        return """
                <table role="presentation" width="100%%" style="margin:0 0 22px;border:2px solid %s;border-radius:16px;background:%s;"><tr><td style="padding:18px;"><table width="100%%"><tr><td class="stack" width="150" style="padding-right:18px;"><div style="width:150px;height:105px;border-radius:12px;background:#e7dfcc;text-align:center;line-height:105px;color:#687151;font:11px Arial,sans-serif;">IMAGEM DO SERVIÇO</div></td><td class="stack" valign="top"><small style="color:#687151;text-transform:uppercase;letter-spacing:1px;">%s</small><h2 style="margin:5px 0;color:%s;font:700 27px/1.15 Georgia,serif;">%s</h2><p style="margin:0;color:%s;font:15px/1.4 Georgia,serif;">%s</p></td>%s</tr></table><table width="100%%" style="margin-top:18px;"><tr><td class="stack" width="33%%">%s<small>DATA</small><strong>%s</strong><span>(%s)</span></td><td class="stack" width="33%%" style="border-left:1px solid #cdbb8c;padding-left:14px;">%s<small>HORÁRIO</small><strong>%s</strong></td><td class="stack" width="33%%" style="border-left:1px solid #cdbb8c;padding-left:14px;">%s<small>DURAÇÃO</small><strong>%s</strong></td></tr></table></td></tr></table>
                """.formatted(DOURADO, CREME, label, VERDE, esc(d.nomeServico()), VERDE, esc(d.descricaoServico()), code, circle("icon-calendar",50), data.format(d.data()), esc(d.data().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, PT_BR)), circle("icon-horario",50), hora.format(d.horario()), circle("icon-duracao",50), duracao(d.duracaoMinutos()));
    }

    private String payment(EmailAppointmentData d) {
        boolean entrada=d.tipoPagamento()==PaymentType.ENTRADA;
        return """
                <table role="presentation" width="100%%" style="margin:0 0 22px;background:%s;border-radius:16px;"><tr><td style="padding:18px 22px;"><small>DETALHES DO PAGAMENTO</small><table width="100%%"><tr><td class="stack" width="50%%">%s<div style="color:%s;font:14px Arial,sans-serif;">%s</div><strong style="color:%s;font:700 27px Georgia,serif;">%s</strong><div style="font:14px Georgia,serif;">%s</div></td><td class="stack" width="50%%" style="border-left:1px solid #d6cfba;padding-left:18px;">%s</td></tr></table></td></tr></table>
                """.formatted(CARD,circle("icon-pagamento",50),VERDE,entrada?"Entrada paga":"Pagamento realizado",VERDE,money(d.valorPago()),entrada?((d.valorPago()!=null&&d.valorServico()!=null&&d.valorServico().signum()!=0)?d.valorPago().multiply(java.math.BigDecimal.valueOf(100)).divide(d.valorServico(),0,java.math.RoundingMode.HALF_UP)+"% do valor do serviço":"Entrada do serviço"):"Valor total do serviço",entrada?"<div>"+circle("icon-copy",50)+"<div style='color:"+VERDE+";font:14px Arial,sans-serif;'>Restante no atendimento</div><strong style='color:"+VERDE+";font:700 27px Georgia,serif;'>"+money(d.valorRestante())+"</strong><div style='font:14px Georgia,serif;'>A ser pago presencialmente</div></div>":"");
    }

    private String info(String title,String main,EmailInfoItem... items){
        var body=java.util.Arrays.stream(items).map(i->"<td class='stack' width='"+(100/items.length)+"%%' align='center' style='padding:0 10px;'>"+circle(i.asset(),52)+"<div style='margin-top:10px;color:"+VERDE+";font:16px/1.35 Georgia,serif;'>"+esc(i.texto())+"</div></td>").collect(Collectors.joining());
        return "<table role='presentation' width='100%%' style='margin:0 0 22px;background:"+CARD+";border-radius:16px;'><tr><td style='padding:18px;'><div style='color:"+VERDE+";font:15px Arial,sans-serif;letter-spacing:1px;text-transform:uppercase;'>"+esc(title)+"</div><table width='100%%' style='margin-top:16px;'><tr>"+body+"</tr></table></td></tr></table>";
    }

    private String twoInfo(String a,String b,String c,String d,String e,String f){return "<table width='100%%' style='margin:0 0 24px;'><tr><td class='stack' width='50%%' style='padding-right:18px;'>"+infoItem(a,b,c)+"</td><td class='stack' width='50%%' style='padding-left:18px;border-left:1px solid #d9d0b7;'>"+infoItem(d,e,f)+"</td></tr></table>";}
    private String infoItem(String asset,String a,String b){return "<table width='100%%'><tr><td width='58'>"+circle(asset,48)+"</td><td style='padding-left:12px;color:"+VERDE+";font:18px/1.45 Georgia,serif;'>"+esc(a)+"<br><strong>"+esc(b)+"</strong></td></tr></table>";}
    private String block(String asset,String title,String text){return "<table width='100%%' style='margin:0 0 22px;background:"+CARD+";border-radius:16px;'><tr><td style='padding:22px;'><table width='100%%'><tr><td width='76'>"+circle(asset,58)+"</td><td><strong style='color:"+VERDE+";font:700 19px/1.35 Georgia,serif;'>"+esc(title)+"</strong><p style='margin:5px 0 0;color:#35412e;font:15px/1.5 Arial,sans-serif;'>"+esc(text)+"</p></td></tr></table></td></tr></table>";}
    private String timeline(EmailRefundData d){var cells=d.etapas().stream().map(s->"<td class='stack' align='center' style='padding:0 7px;'><div style='margin:auto;width:42px;height:42px;border-radius:50%%;background:"+(s.concluida()?VERDE_FUNDO:"#e5e6d7")+";color:"+(s.concluida()?CREME:"#687151")+";font:700 22px/42px Arial;text-align:center;'>"+(s.concluida()?"✓":"")+"</div><strong style='display:block;margin-top:8px;color:"+VERDE+";font:700 15px/1.25 Georgia,serif;'>"+esc(s.titulo())+"</strong><span style='display:block;margin-top:5px;font:13px/1.35 Georgia,serif;'>"+esc(s.descricao())+"</span></td>").collect(Collectors.joining());return "<table width='100%%' style='margin:0 0 18px;background:"+CARD+";border-radius:16px;'><tr><td style='padding:18px 10px 22px;'><div style='color:"+VERDE+";font:15px Arial,sans-serif;letter-spacing:1px;text-transform:uppercase;margin-bottom:18px;'>Status do reembolso</div><table width='100%%'><tr>"+cells+"</tr></table></td></tr></table>";}
    private String refund(String title,String text){return "<table width='100%%' style='margin:0 0 18px;background:#f7e8e4;border-radius:14px;'><tr><td width='70' align='center'>"+circle("icon-horario",48)+"</td><td style='border-left:1px solid #cdbb8c;padding:14px 18px;'><strong style='color:"+VERDE+";font:14px Arial,sans-serif;letter-spacing:1px;'>"+esc(title)+"</strong><div style='margin-top:4px;font:15px/1.4 Georgia,serif;'>"+esc(text)+"</div></td></tr></table>";}
    private String cta(String text,String href,String asset){return "<table width='100%%' style='margin:4px 0 26px;'><tr><td align='center'><a href='"+(href==null?"#":esc(href))+"' style='display:inline-block;min-width:300px;padding:15px 24px;background:"+VERDE_FUNDO+";border:1px solid "+DOURADO+";border-radius:32px;color:"+CREME+";font:15px Arial,sans-serif;letter-spacing:1px;text-decoration:none;'>"+icon(asset,22)+"&nbsp;&nbsp;"+esc(text)+" →</a></td></tr></table>";}
    private String closing(){return "<table width='100%%' style='margin:0 0 12px;'><tr><td width='42%%' style='border-top:1px solid #cdbb8c;'></td><td width='16%%' align='center'>"+circle("icon-assinatura",38)+"</td><td width='42%%' style='border-top:1px solid #cdbb8c;'></td></tr></table><div style='text-align:center;'><p style='margin:0 0 4px;color:"+VERDE+";font:22px/1.35 Georgia,serif;'>Cuidar de você é a nossa essência.</p><p style='margin:0;color:#687151;font:14px Arial,sans-serif;'>Julyana Lima — Estética e Bem-estar</p></div><table width='100%%' style='margin-top:24px;'><tr>"+service("icon-estetica","Estética","Facial e Corporal")+service("icon-massoterapia","Massoterapia","")+service("icon-relaxamento","Relaxamento","")+service("icon-bem-estar","Bem-estar","")+"</tr></table>";}
    private String footer(){return "<tr><td style=\"padding:0;background:"+VERDE_FUNDO+";height:132px;background-image:url('cid:footer-background');background-position:center bottom;background-size:100%% auto;background-repeat:no-repeat;\"><table width=\"100%%\" height=\"132\"><tr><td class=\"footer-col\" width=\"50%%\" valign=\"bottom\" style=\"padding:0 22px 22px 34px;border-right:1px solid rgba(247,241,229,.55);\"><p style=\"margin:0 0 10px;color:"+CREME+";font:16px Georgia,serif;\">Siga nossas redes</p><a href=\"#\" style=\"color:"+CREME+";\">Instagram</a>&nbsp;&nbsp;<a href=\"#\" style=\"color:"+CREME+";\">WhatsApp</a></td><td class=\"footer-col\" width=\"50%%\" valign=\"bottom\" style=\"padding:0 34px 22px 22px;color:"+CREME+";font:14px Arial,sans-serif;\"><strong>Feira de Santana - BA</strong><br><small>Beleza, saúde e bem-estar em um só lugar.</small></td></tr></table></td></tr>";}
    private String service(String asset,String a,String b){return "<td class='service' width='25%%' align='center'>"+circle(asset,58)+"<div style='color:#687151;font:13px/1.35 Arial,sans-serif;'>"+esc(a)+(b.isBlank()?"":"<br>"+esc(b))+"</div></td>";}
    private String duracao(int m){if(m<=0)return "—";int h=m/60,r=m%60;return h==0?r+"min":r==0?h+"h":h+"h"+r;}
    private String circle(String asset,int size){return "<div data-asset='"+esc(asset)+"' style='width:"+size+"px;height:"+size+"px;border-radius:50%%;background:"+ICONE+";display:inline-flex;align-items:center;justify-content:center;overflow:hidden;'><img src='cid:"+esc(asset)+"' width='"+size+"' height='"+size+"' alt='' style='display:block;width:78%%;height:78%%;object-fit:contain;border:0;'></div>";}
    private String icon(String asset,int size){return "<span data-asset='"+esc(asset)+"' style='display:inline-block;width:"+size+"px;height:"+size+"px;vertical-align:middle;'><img src='cid:"+esc(asset)+"' width='"+size+"' height='"+size+"' alt='' style='display:block;width:100%%;height:100%%;object-fit:contain;border:0;'></span>";}
    private String money(java.math.BigDecimal value){return value==null?"—":NumberFormat.getCurrencyInstance(PT_BR).format(value);}
    private String esc(String s){if(s==null)return "";return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
}
