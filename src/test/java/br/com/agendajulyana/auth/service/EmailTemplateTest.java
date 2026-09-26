package br.com.agendajulyana.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateTest {

    private final EmailTemplate template = new EmailTemplate();

    @Test
    void deveGerarTemplateDeRecuperacaoComEstruturaVisualBase() {
        var html = template.recuperarSenha("Maria", "483921");

        assertAll(
                () -> assertTrue(html.contains("cid:julyana-email-header")),
                () -> assertTrue(html.contains("Olá, Maria!")),
                () -> assertTrue(html.contains("483921")),
                () -> assertTrue(html.contains("15 minutos")),
                () -> assertTrue(html.contains("uma única vez")),
                () -> assertTrue(html.contains("Julyana Lima — Estética e Bem-estar")),
                () -> assertTrue(html.contains("Siga nossas redes")),
                () -> assertTrue(html.contains("Feira de Santana - BA"))
        );
    }

    @Test
    void devePrepararCirculosParaAssetsDosBeneficiosEServicos() {
        var html = template.recuperarSenha("Maria", "483921");

        assertAll(
                () -> assertTrue(html.contains("ASSET: icon-validade")),
                () -> assertTrue(html.contains("ASSET: icon-uso-unico")),
                () -> assertTrue(html.contains("ASSET: icon-seguranca")),
                () -> assertTrue(html.contains("ASSET: icon-estetica")),
                () -> assertTrue(html.contains("ASSET: icon-massoterapia")),
                () -> assertTrue(html.contains("ASSET: icon-relaxamento")),
                () -> assertTrue(html.contains("ASSET: icon-bem-estar")),
                () -> assertTrue(html.contains("border-radius:50%"))
        );
    }

    @Test
    void devePrepararAcaoVisualDeCopiarCodigo() {
        var html = template.recuperarSenha("Maria", "483921");

        assertAll(
                () -> assertTrue(html.contains("COPIAR<br>CÓDIGO")),
                () -> assertTrue(html.contains("<svg width=\"28\" height=\"28\"")),
                () -> assertTrue(html.contains("aria-hidden=\"true\""))
        );
    }

    @Test
    void deveManterLayoutResponsivoParaMobile() {
        var html = template.recuperarSenha("Maria", "483921");

        assertAll(
                () -> assertTrue(html.contains("max-width:480px")),
                () -> assertTrue(html.contains(".email-content")),
                () -> assertTrue(html.contains(".service-column")),
                () -> assertTrue(html.contains("color-scheme"))
        );
    }

    @Test
    void deveEscaparDadosInseridosNoHtml() {
        var html = template.recuperarSenha("<teste>", "12&34");

        assertTrue(html.contains("12&amp;34"));
        assertFalse(html.contains("<teste>"));
    }
}
