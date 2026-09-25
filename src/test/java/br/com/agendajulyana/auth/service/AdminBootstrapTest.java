package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.*;
import br.com.agendajulyana.auth.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminBootstrapTest {

    @Mock UsuarioRepository usuarios;
    @Mock PapelRepository papeis;
    @Mock IdentidadeAutenticacaoRepository identidades;
    @Mock org.springframework.security.crypto.password.PasswordEncoder encoder;

    @Test
    void deveCriarPrimeiroAdminQuandoNaoExisteAdmin() {
        var bootstrap = criarBootstrap();
        when(usuarios.existsByPapelNome(PapelNome.ADMIN)).thenReturn(false);
        when(usuarios.existsByEmailIgnoreCase("admin@email.com")).thenReturn(false);
        when(papeis.findByNome(PapelNome.ADMIN)).thenReturn(Optional.of(new Papel(PapelNome.ADMIN)));
        when(encoder.encode("senha123")).thenReturn("hash");

        bootstrap.run();

        verify(usuarios).save(any(Usuario.class));
        verify(identidades).save(any(IdentidadeAutenticacao.class));
    }

    @Test
    void naoDeveCriarAdminSeJaExiste() {
        var bootstrap = criarBootstrap();
        when(usuarios.existsByPapelNome(PapelNome.ADMIN)).thenReturn(true);

        bootstrap.run();

        verify(usuarios, never()).save(any());
        verify(identidades, never()).save(any());
    }

    @Test
    void naoDeveExecutarQuandoDesabilitado() {
        var bootstrap = criarBootstrap();
        ReflectionTestUtils.setField(bootstrap, "enabled", false);

        bootstrap.run();

        verifyNoInteractions(usuarios, papeis, identidades, encoder);
    }

    @Test
    void deveExigirDadosQuandoHabilitado() {
        var bootstrap = criarBootstrap();
        ReflectionTestUtils.setField(bootstrap, "name", "");

        when(usuarios.existsByPapelNome(PapelNome.ADMIN)).thenReturn(false);

        assertThrows(IllegalStateException.class, bootstrap::run);

        verify(usuarios, never()).save(any());
        verify(identidades, never()).save(any());
    }

    private AdminBootstrap criarBootstrap() {
        var bootstrap = new AdminBootstrap(usuarios, papeis, identidades, encoder);
        ReflectionTestUtils.setField(bootstrap, "enabled", true);
        ReflectionTestUtils.setField(bootstrap, "name", "Admin Inicial");
        ReflectionTestUtils.setField(bootstrap, "email", "ADMIN@EMAIL.COM");
        ReflectionTestUtils.setField(bootstrap, "phone", "75999999999");
        ReflectionTestUtils.setField(bootstrap, "password", "senha123");
        return bootstrap;
    }
}
