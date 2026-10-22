package br.com.agendajulyana.integration.whatsapp;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BaileysMessageClientTest {

    @Test
    void deveEnviarCodigoPeloGatewayBaileys() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://baileys.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("http://baileys.test/v1/messages"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer baileys-token"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"phone\":\"5575999999999\",\"message\":\"Seu código de verificação é: 123456\"}"))
                .andRespond(withSuccess());

        new BaileysMessageClient(builder, "baileys-token")
                .sendVerificationCode("75999999999", "123456");

        server.verify();
    }

    @Test
    void deveFalharSeTokenNaoEstiverConfigurado() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://baileys.test");
        var client = new BaileysMessageClient(builder, " ");

        assertThrows(
                IllegalStateException.class,
                () -> client.sendVerificationCode("5575999999999", "123456")
        );
    }
}