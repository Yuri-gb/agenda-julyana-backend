package br.com.agendajulyana.integration.whatsapp;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class WhatsAppWebhookControllerTest {

    private final MockMvc mvc = standaloneSetup(
            new WhatsAppWebhookController("whatsapp-test-token")
    ).build();

    @Test
    void deveResponderAoDesafioDoMetaQuandoTokenForValido() throws Exception {
        mvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "whatsapp-test-token")
                        .param("hub.challenge", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("123456"));
    }

    @Test
    void deveRecusarDesafioQuandoTokenForInvalido() throws Exception {
        mvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "token-incorreto")
                        .param("hub.challenge", "123456"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRecusarDesafioQuandoModoNaoForSubscribe() throws Exception {
        mvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "other")
                        .param("hub.verify_token", "whatsapp-test-token")
                        .param("hub.challenge", "123456"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveAceitarEventoPostDoWhatsApp() throws Exception {
        mvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "object": "whatsapp_business_account",
                                  "entry": []
                                }
                                """))
                .andExpect(status().isOk());
    }
}
