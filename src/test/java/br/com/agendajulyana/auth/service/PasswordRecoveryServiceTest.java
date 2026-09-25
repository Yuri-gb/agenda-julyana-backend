package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.*;
import br.com.agendajulyana.auth.dto.*;
import br.com.agendajulyana.auth.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock UsuarioRepository usuarios;
    @Mock IdentidadeAutenticacaoRepository identidades;
    @Mock RecuperacaoSenhaRepository recuperacoes;
    @Mock PasswordEncoder encoder;
    @Mock EmailService emailService;

    @Test
    void deveResponderDeFormaGenericaParaEmailInexistente() {
        var service = service();
        when(usuarios.findByEmailIgnoreCase("naoexiste@email.com")).thenReturn(Optional.empty());

        var response = service.solicitar("naoexiste@email.com");

        assertTrue(response.mensagem().contains("Se o e-mail estiver cadastrado"));
        verifyNoInteractions(emailService);
    }

    @Test
    void naoDeveEnviarCodigoParaContaSomenteGoogle() {
        var service = service();
        var usuario = new Usuario("Google", "google@email.com", null);
        when(usuarios.findByEmailIgnoreCase("google@email.com")).thenReturn(Optional.of(usuario));
        when(identidades.existsByUsuarioIdAndProvedor(usuario.getId(), ProvedorAutenticacao.LOCAL))
            .thenReturn(false);

        service.solicitar("google@email.com");

        verifyNoInteractions(emailService);
        verify(recuperacoes, never()).save(any());
    }

    @Test
    void deveCriarRecuperacaoEEnviarCodigoParaContaLocal() {
        var service = service();
        var usuario = new Usuario("Cliente", "cliente@email.com", null);
        when(usuarios.findByEmailIgnoreCase("cliente@email.com")).thenReturn(Optional.of(usuario));
        when(identidades.existsByUsuarioIdAndProvedor(usuario.getId(), ProvedorAutenticacao.LOCAL))
            .thenReturn(true);
        when(encoder.encode(anyString())).thenReturn("codigo-hash");

        service.solicitar("cliente@email.com");

        verify(recuperacoes).save(any(RecuperacaoSenha.class));
        verify(emailService).enviarCodigoRecuperacao(
            eq("cliente@email.com"), eq("Cliente"), anyString()
        );
    }

    private PasswordRecoveryService service() {
        return new PasswordRecoveryService(
            usuarios, identidades, recuperacoes, encoder, emailService
        );
    }
}
