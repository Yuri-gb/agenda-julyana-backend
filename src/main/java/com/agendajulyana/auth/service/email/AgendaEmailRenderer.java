package br.com.agendajulyana.auth.service.email;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

import br.com.agendajulyana.auth.service.email.EmailData.EmailAppointmentData;
import br.com.agendajulyana.auth.service.email.EmailData.EmailInfoItem;
import br.com.agendajulyana.auth.service.email.EmailData.EmailRefundData;
import br.com.agendajulyana.auth.service.email.EmailData.PaymentType;

@Component
public final class AgendaEmailRenderer {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final String VERDE = "#20351f";
    private static final String VERDE_FUNDO = "#17351c";
    private static final String CREME = "#f7f1e5";
    private static final String CARD = "#efeedf";
    private static final String ICONE = CARD;
    private static final String DOURADO = "#b38a32";

    private final DateTimeFormatter data = DateTimeFormatter.ofPattern("dd 'de' MMM 'de' yyyy", PT_BR);
    private final DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm");

    public String recuperarSenha(String nome, String codigo) {
        return layout(
                "<h1 style=\"margin:0 0 18px;color:" + VERDE + ";font:400 48px/1.05 Georgia,serif;\">Olá, " + esc(primeirosNomes(nome)) + "!</h1>"
                + "<p style=\"margin:0 0 26px;color:" + VERDE + ";font:18px/1.55 Georgia,serif;\">Recebemos uma solicitação para redefinir a senha da sua conta na Agenda Julyana.</p>"
                + "<p style=\"margin:0 0 12px;color:#687151;font:18px/1.5 Georgia,serif;\">Use o código abaixo para continuar:</p>"
                + "<table role=\"presentation\" width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin:0 0 28px;\"><tr><td style=\"padding:22px 28px;border:2px solid " + DOURADO + ";border-radius:18px;background:" + CREME + ";\"><table role=\"presentation\" width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\"><strong style=\"color:" + VERDE + ";font:700 48px/1.05 Arial,sans-serif;letter-spacing:8px;\">" + esc(codigo) + "</strong></td><td width=\"118\" align=\"center\" style=\"border-left:1px solid #d7c796;padding-left:12px;\">" + icon("icon-copy", 58) + "<div style=\"font:12px/1.15 Arial,sans-serif;color:" + DOURADO + ";font-weight:700;letter-spacing:.15px;\">COPIAR<br>CÓDIGO</div>":"");
    }

    private String info(String title,String main,EmailInfoItem... items){
        var body=java.util.Arrays.stream(items).map(i->"<td class='stack' width='"+(100/items.length)+"%%' align='center' style='padding:0 10px;'>"+circle(i.asset(),52)+"<div style='margin-top:10px;color:"+VERDE+";font:16px/1.35 Georgia,serif;'>"+esc(i.texto())+"</div></td>").collect(Collectors.joining());
        return "<table role='presentation' width='100%%' style='margin:0 0 22px;background:"+CARD+";border-radius:16px;'><tr><td style='padding:18px;'><div style='color:"+VERDE+";font:15px Arial,sans-serif;letter-spacing:1px;text-transform:uppercase;'>"+esc(title)+"</div><table width='100%%' style='margin-top:16px;'><tr>"+body+"</tr></table></td></tr></table>";
    }

    private String twoInfo(String a,String b,String c,String d,String e,String f){return "<table width='100%%' style='margin:0 0 24px;'><tr><td class='stack' width='50%%' style='padding-right:18px;'>"+infoItem(a,b,c)+"</td><td class='stack' width='50%%' style='padding-left:18px;border-left:1px solid #d9d0b7;'>"+infoItem(d,e,f)+"</td></tr></table>";}
    private String infoItem(String asset,String a,String b){return "<table width='100%%'><tr><td width='68'>"+circle(asset,58)+"</td><td style='padding-left:12px;color:"+VERDE+";font:16px/1.35 Georgia,serif;'>"+esc(a)+"<br><strong>"+esc(b)+"</strong></td></tr></table>";}
    private String block(String asset,String title,String text){return "<table width='100%%' style='margin:0 0 22px;background:"+CARD+";border-radius:16px;'><tr><td style='padding:22px;'><table width='100%%'><tr><td width='68'>"+circle(asset,58)+"</td><td><strong style='color:"+VERDE+";font:700 19px/1.35 Georgia,serif;'>"+esc(title)+"</strong><p style='margin:5px 0 0;color:#35412e;font:15px/1.5 Arial,sans-serif;'>"+esc(text)+"</p></td></tr></table></td></tr></table>";}
    private String timeline(EmailRefundData d){var cells=d.etapas().stream().map(s->"<td class='stack' align='center' style='padding:0 7px;'><div style='margin:auto;width:42px;height:42px;border-radius:50%%;background:"+(s.concluida()?VERDE_FUNDO:"#e5e6d7")+";color:"+(s.concluida()?CREME:"#687151")+";font:700 22px/42px Arial;text-align:center;'>"+(s.concluida()?"✓":"")+"</div><strong style='display:block;margin-top:8px;color:"+VERDE+";font:700 15px/1.25 Georgia,serif;'>"+esc(s.titulo())+"</strong><span style='display:block;margin-top:5px;font:13px/1.35 Georgia,serif;'>"+esc(s.descricao())+"</span></td>").collect(Collectors.joining());return "<table width='100%%' style='margin:0 0 18px;background:"+CARD+";border-radius:16px;'><tr><td style='padding:18px 10px 22px;'><div style='color:"+VERDE+";font:15px Arial,sans-serif;letter-spacing:1px;text-transform:uppercase;margin-bottom:18px;'>Status do reembolso</div><table width='100%%'><tr>"+cells+"</tr></table></td></tr></table>";}
    private String refund(String title,String text){return "<table width='100%%' style='margin:0 0 18px;background:#f7e8e4;border-radius:14px;'><tr><td width='70' align='center'>"+circle("icon-horario",48)+"</td><td style='border-left:1px solid #cdbb8c;padding:14px 18px;'><strong style='color:"+VERDE+";font:14px Arial,sans-serif;letter-spacing:1px;'>"+esc(title)+"</strong><div style='margin-top:4px;font:15px/1.4 Georgia,serif;'>"+esc(text)+"</div></td></tr></table>";}
    private String cta(String text,String href,String asset){return "<table width='100%%' style='margin:4px 0 26px;'><tr><td align='center'><a href='"+(href==null?"#":esc(href))+"' style='display:inline-block;min-width:300px;padding:15px 24px;background:"+VERDE_FUNDO+";border:1px solid "+DOURADO+";border-radius:32px;color:"+CREME+";font:15px Arial,sans-serif;letter-spacing:1px;text-decoration:none;'>"+icon(asset,22)+"&nbsp;&nbsp;"+esc(text)+" →</a></td></tr></table>";}
    private String closing(){return "<table width='100%%' style='margin:0 0 12px;'><tr><td width='42%%' style='border-top:1px solid #cdbb8c;'></td><td width='16%%' align='center'>"+circle("icon-assinatura",44)+"</td><td width='42%%' style='border-top:1px solid #cdbb8c;'></td></tr></table><div style='text-align:center;'><p style='margin:0 0 4px;color:"+VERDE+";font:22px/1.35 Georgia,serif;'>Cuidar de você é a nossa essência.</p><p style='margin:0;color:#687151;font:14px Arial,sans-serif;'>Julyana Lima — Estética e Bem-estar</p></div><table width='100%%' style='margin-top:24px;'><tr>"+service("icon-estetica","Estética","Facial e Corporal")+service("icon-massoterapia","Massoterapia","")+service("icon-relaxamento","Relaxamento","")+service("icon-bem-estar","Bem-estar","")+"</tr></table>";}
    private String footer(){return "<tr><td style=\"padding:0;line-height:0;background:"+VERDE_FUNDO+";\"><img src=\"cid:footer-background\" width=\"700\" alt=\"Siga nossas redes — Feira de Santana - BA\" style=\"display:block;width:100%%;height:auto;border:0;\"></td></tr>";}
    private String service(String asset,String a,String b){return "<td class='service' width='25%%' align='center'>"+circle(asset,58)+"<div style='color:#687151;font:13px/1.35 Arial,sans-serif;'>"+esc(a)+(b.isBlank()?"":"<br>"+esc(b))+"</div></td>";}
    private String duracao(int m){if(m<=0)return "—";int h=m/60,r=m%60;return h==0?r+"min":r==0?h+"h":h+"h"+r;}
    private String circle(String asset,int size){
        var scale = "icon-horario".equals(asset) ? 0.88 : 0.78;
        var iconSize = Math.max(1, (int) Math.round(size * scale));
        return "<table data-asset='"+esc(asset)+"' role='presentation' cellpadding='0' cellspacing='0' border='0' width='"+size+"' height='"+size+"' style='width:"+size+"px;height:"+size+"px;border-collapse:collapse;'>"
                + "<tr><td width='"+size+"' height='"+size+"' align='center' valign='middle' bgcolor='"+ICONE+"' style='width:"+size+"px;height:"+size+"px;background:"+ICONE+";border-radius:50%;text-align:center;vertical-align:middle;overflow:hidden;'>"
                + "<img src='cid:"+esc(asset)+"' width='"+iconSize+"' height='"+iconSize+"' alt='' style='display:block;width:"+iconSize+"px;height:"+iconSize+"px;margin:0 auto;object-fit:contain;border:0;'>"
                + "</td></tr></table>";
    }
    private String icon(String asset,int size){return "<span data-asset='"+esc(asset)+"' style='display:inline-block;width:"+size+"px;height:"+size+"px;vertical-align:middle;'><img src='cid:"+esc(asset)+"' width='"+size+"' height='"+size+"' alt='' style='display:block;width:100%%;height:100%%;object-fit:contain;border:0;'></span>";}
    private String primeirosNomes(String nome){
        if(nome==null || nome.isBlank()) return "";
        var partes=nome.trim().split("\\s+");
        if(partes.length<=2) return nome.trim();
        return partes[0]+" "+partes[1];
    }
    private String money(java.math.BigDecimal value){return value==null?"—":NumberFormat.getCurrencyInstance(PT_BR).format(value);}
    private String esc(String s){if(s==null)return "";return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
}
