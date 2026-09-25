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
    void deveValidarCodigoEEmitirTokenDeRedefinicao() {
        var service = service();
        var usuario = new Usuario("Cliente", "cliente@email.com", null);
        var recuperacao = new RecuperacaoSenha(usuario, "codigo-hash", OffsetDateTime.now().plusMinutes(15));

        when(usuarios.findByEmailIgnoreCase("cliente@email.com")).thenReturn(Optional.of(usuario));
        when(recuperacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(usuario.getId())).thenReturn(Optional.of(recuperacao));
        when(encoder.matches("123456", "codigo-hash")).thenReturn(true);

        var response = service.verificarCodigo("cliente@email.com", "123456");

        assertNotNull(response.tokenRedefinicao());
        assertEquals(600, response.expiraEmSegundos());
        assertEquals(1, recuperacao.getTentativas());
        assertNotNull(recuperacao.getVerificadoEm());
        assertNotNull(recuperacao.getTokenRedefinicaoHash());
    }

    @Test
    void deveRejeitarCodigoIncorreto() {
        var service = service();
        var usuario = new Usuario("Cliente", "cliente@email.com", null);
        var recuperacao = new RecuperacaoSenha(usuario, "codigo-hash", OffsetDateTime.now().plusMinutes(15));

        when(usuarios.findByEmailIgnoreCase("cliente@email.com")).thenReturn(Optional.of(usuario));
        when(recuperacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(usuario.getId())).thenReturn(Optional.of(recuperacao));
        when(encoder.matches("000000", "codigo-hash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.verificarCodigo("cliente@email.com", "000000"));
        assertEquals(1, recuperacao.getTentativas());
        assertNull(recuperacao.getVerificadoEm());
    }

    @Test
    void deveBloquearCodigoAposCincoTentativas() {
        var service = service();
        var usuario = new Usuario("Cliente", "cliente@email.com", null);
        var recuperacao = new RecuperacaoSenha(usuario, "codigo-hash", OffsetDateTime.now().plusMinutes(15));
        for (int i = 0; i < 5; i++) recuperacao.registrarTentativa();

        when(usuarios.findByEmailIgnoreCase("cliente@email.com")).thenReturn(Optional.of(usuario));
        when(recuperacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(usuario.getId())).thenReturn(Optional.of(recuperacao));

        assertThrows(IllegalArgumentException.class, () -> service.verificarCodigo("cliente@email.com", "123456"));
        verifyNoInteractions(encoder);
    }

    @Test
    void deveRejeitarCodigoExpirado() {
        var service = service();
        var usuario = new Usuario("Cliente", "cliente@email.com", null);
        var recuperacao = new RecuperacaoSenha(usuario, "codigo-hash", OffsetDateTime.now().minusMinutes(1));

        when(usuarios.findByEmailIgnoreCase("cliente@email.com")).thenReturn(Optional.of(usuario));
        when(recuperacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(usuario.getId())).thenReturn(Optional.of(recuperacao));

        assertThrows(IllegalArgumentException.class, () -> service.verificarCodigo("cliente@email.com", "123456"));
        verifyNoInteractions(encoder);
    }

    @Test
    void deveRedefinirSenhaEConsumirRecuperacao() {
        var service = service();
        var usuario = new Usuario("Cliente", "cliente@email.com", null);
        var recuperacao = new RecuperacaoSenha(usuario, "codigo-hash", OffsetDateTime.now().plusMinutes(15));
        recuperacao.verificar("token-hash", OffsetDateTime.now().plusMinutes(10));
        var identidade = IdentidadeAutenticacao.local(usuario, usuario.getEmail(), "senha-antiga");

        when(recuperacoes.findByTokenRedefinicaoHashAndUtilizadoEmIsNull("token-hash")).thenReturn(Optional.of(recuperacao));
        when(identidades.findByProvedorAndIdentificadorExterno(ProvedorAutenticacao.LOCAL, usuario.getEmail().toLowerCase())).thenReturn(Optional.of(identidade));
        when(encoder.encode("nova-senha")).thenReturn("nova-senha-hash");

        var response = service.redefinirSenha(new ResetPasswordRequest("token", "nova-senha", "nova-senha"));

        assertEquals("Senha redefinida com sucesso.", response.mensagem());
        assertNotNull(recuperacao.getUtilizadoEm());
        assertEquals("nova-senha-hash", identidade.getSenhaHash());
        verify(identidades).save(identidade);
    }

    @Test
    void deveRejeitarTokenDeRedefinicaoExpirado() {
        var service = service();
        var usuario = new Usuario("Cliente", "cliente@email.com", null);
        var recuperacao = new RecuperacaoSenha(usuario, "codigo-hash", OffsetDateTime.now().plusMinutes(15));
        recuperacao.verificar("token-hash", OffsetDateTime.now().minusMinutes(1));

        when(recuperacoes.findByTokenRedefinicaoHashAndUtilizadoEmIsNull("token-hash")).thenReturn(Optional.of(recuperacao));

        assertThrows(IllegalArgumentException.class, () -> service.redefinirSenha(new ResetPasswordRequest("token", "nova-senha", "nova-senha")));
        verify(identidades, never()).save(any());
    }

    @Test
    void deveRejeitarSenhasDiferentes() {
        var service = service();

        assertThrows(IllegalArgumentException.class, () -> service.redefinirSenha(new ResetPasswordRequest("token", "nova-senha", "outra-senha")));
        verifyNoInteractions(recuperacoes, identidades, encoder);
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
