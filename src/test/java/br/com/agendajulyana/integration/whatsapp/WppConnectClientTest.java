package br.com.agendajulyana.integration.whatsapp;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WppConnectClientTest {
    @Test
    void deveEnviarCodigoPeloEndpointDoWppConnect() {
        RestClient client = RestClient.builder().baseUrl("http://wppconnect.test").build();
        MockRestServiceServer server = MockRestServiceServer.bindTo(client).build();

        server.expect(requestTo("http://wppconnect.test/api/agenda-julyana/send-message"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer wpp-token"))
                .andExpect(content().json("""
                        {"phone":"5575999999999","message":"Seu código de verificação é: 123456"}
                        """))
                .andRespond(withSuccess());

        new WppConnectClient(client, "wpp-token", "agenda-julyana")
                .sendVerificationCode("5575999999999", "123456");

        server.verify();
    }

    @Test
    void deveConsultarStatusDaSessao() {
        RestClient client = RestClient.builder().baseUrl("http://wppconnect.test").build();
        MockRestServiceServer server = MockRestServiceServer.bindTo(client).build();

        server.expect(requestTo("http://wppconnect.test/api/agenda-julyana/status-session"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer wpp-token"))
                .andRespond(withSuccess("{\"status\":\"CONNECTED\"}", org.springframework.http.MediaType.APPLICATION_JSON));

        new WppConnectClient(client, "wpp-token", "agenda-julyana").statusSession();
        server.verify();
    }
}
