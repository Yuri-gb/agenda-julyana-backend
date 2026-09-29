package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.domain.VerificacaoTelefone;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.auth.repository.VerificacaoTelefoneRepository;
import br.com.agendajulyana.integration.whatsapp.WhatsAppMessageSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.OffsetDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacaoTelefoneServiceTest {
    @Mock UsuarioRepository usuarios;
    @Mock VerificacaoTelefoneRepository verificacoes;
    @Mock WhatsAppMessageSender whatsapp;

    @Test
    void deveCriarCodigoHashEEnviarCodigo() {
        Usuario usuario = new Usuario("Yuri", "yuri@example.com", "75999999999");
        when(usuarios.findByEmailIgnoreCase("yuri@example.com")).thenReturn(Optional.of(usuario));
        when(verificacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(any())).thenReturn(Optional.empty());

        var service = new VerificacaoTelefoneService(usuarios, verificacoes, whatsapp);
        service.solicitar("yuri@example.com", "75988887777");

        ArgumentCaptor<VerificacaoTelefone> captor = ArgumentCaptor.forClass(VerificacaoTelefone.class);
        verify(verificacoes).save(captor.capture());
        verify(whatsapp).sendVerificationCode(eq("75988887777"), anyString());

        VerificacaoTelefone verificacao = captor.getValue();
        assertEquals(64, verificacao.getCodigoHash().length());
        assertTrue(verificacao.getExpiraEm().isAfter(OffsetDateTime.now()));
        assertEquals(0, verificacao.getTentativas());
    }

    @Test
    void deveConfirmarCodigoCorreto() {
        Usuario usuario = new Usuario("Yuri", "yuri@example.com", "75999999999");
        var verificacao = new VerificacaoTelefone(usuario, sha256("123456"), OffsetDateTime.now().plusMinutes(5));
        when(usuarios.findByEmailIgnoreCase("yuri@example.com")).thenReturn(Optional.of(usuario));
        when(verificacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(any())).thenReturn(Optional.of(verificacao));

        var service = new VerificacaoTelefoneService(usuarios, verificacoes, whatsapp);
        var response = service.confirmar("yuri@example.com", "123456");

        assertTrue(response.telefoneVerificado());
        assertTrue(usuario.isTelefoneVerificado());
        assertNotNull(verificacao.getVerificadoEm());
        assertNotNull(verificacao.getUtilizadoEm());
        assertEquals(1, verificacao.getTentativas());
    }

    @Test
    void deveRejeitarCodigoIncorretoESomarTentativa() {
        Usuario usuario = new Usuario("Yuri", "yuri@example.com", "75999999999");
        var verificacao = new VerificacaoTelefone(usuario, sha256("123456"), OffsetDateTime.now().plusMinutes(5));
        when(usuarios.findByEmailIgnoreCase("yuri@example.com")).thenReturn(Optional.of(usuario));
        when(verificacoes.findTopByUsuarioIdAndUtilizadoEmIsNullOrderByCriadoEmDesc(any())).thenReturn(Optional.of(verificacao));

        var service = new VerificacaoTelefoneService(usuarios, verificacoes, whatsapp);

        assertThrows(IllegalStateException.class, () -> service.confirmar("yuri@example.com", "000000"));
        assertEquals(1, verificacao.getTentativas());
        assertFalse(usuario.isTelefoneVerificado());
    }

    private static String sha256(String value) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new AssertionError(e); }
    }
}
