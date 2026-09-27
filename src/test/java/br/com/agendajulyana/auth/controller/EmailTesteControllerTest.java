package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.service.EmailTesteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailTesteControllerTest {
    @Mock
    private EmailTesteService emailTesteService;

    @Test
    void deveDispararEmailPeloTipoSelecionado() {
        var controller = new EmailTesteController(emailTesteService);
        var request = new EmailTesteController.EmailTesteRequest("teste@example.com", "Yuri", "483921");

        var response = controller.enviar(EmailTesteTipo.RECUPERACAO_SENHA, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("E-mail de teste enviado com sucesso.", response.getBody().mensagem());
        verify(emailTesteService).enviar(EmailTesteTipo.RECUPERACAO_SENHA, "teste@example.com", "Yuri", "483921");
    }
}
