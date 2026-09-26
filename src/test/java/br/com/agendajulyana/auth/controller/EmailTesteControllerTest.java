package br.com.agendajulyana.auth.controller;

import br.com.agendajulyana.auth.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailTesteControllerTest {

    @Mock
    private EmailService emailService;

    @Test
    void deveDispararEmailDeRecuperacaoPeloTipoSelecionado() {
        var controller = new EmailTesteController(emailService);
        var request = new EmailTesteController.EmailTesteRequest(
                "teste@example.com",
                EmailTesteController.Tipo.RECUPERACAO_SENHA,
                "Yuri",
                "483921"
        );

        var response = controller.enviar(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("E-mail de teste enviado com sucesso.", response.getBody().mensagem());
        verify(emailService).enviarCodigoRecuperacao("teste@example.com", "Yuri", "483921");
    }
}
