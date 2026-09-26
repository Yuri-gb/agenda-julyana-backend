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
                () -> assertTrue(html.contains("Olá!")),
                () -> assertTrue(html.contains("483921")),
                () -> assertTrue(html.contains("15 minutos")),
                () -> assertTrue(html.contains("uma única vez")),
                () -> assertTrue(html.contains("Julyana Lima — Estética e Bem-estar"))
        );
    }

    @Test
    void deveEscaparCodigoInseridoNoHtml() {
        var html = template.recuperarSenha("<teste>", "12&34");

        assertTrue(html.contains("12&amp;34"));
        assertFalse(html.contains("<teste>"));
    }
}
