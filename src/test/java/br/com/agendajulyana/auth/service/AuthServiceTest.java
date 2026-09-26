package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.*;
import br.com.agendajulyana.auth.dto.*;
import br.com.agendajulyana.auth.repository.*;
import br.com.agendajulyana.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarios;
    @Mock PapelRepository papeis;
    @Mock IdentidadeAutenticacaoRepository identidades;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @Mock ClienteRepository clientes;

    @Test
    void deveCadastrarClienteComCredencialLocal() {
        var service = new AuthService(usuarios, papeis, identidades, encoder, jwt, clientes);
        var papel = new Papel(PapelNome.CLIENTE);

        when(usuarios.existsByEmailIgnoreCase("cliente@email.com")).thenReturn(false);
        when(papeis.findByNome(PapelNome.CLIENTE)).thenReturn(java.util.Optional.of(papel));
        when(encoder.encode("senha123")).thenReturn("hash");

        var response = service.register(new RegisterRequest(
            "Cliente", "cliente@email.com", "75999999999", "senha123"
        ));

        assertEquals("Cliente", response.nome());
        assertEquals("cliente@email.com", response.email());
        assertEquals("CLIENTE", response.papeis().iterator().next());
        verify(usuarios).save(any(Usuario.class));
        verify(identidades).save(any(IdentidadeAutenticacao.class));
    }

    @Test
    void deveRecusarCadastroComEmailExistente() {
        var service = new AuthService(usuarios, papeis, identidades, encoder, jwt, clientes);
        when(usuarios.existsByEmailIgnoreCase("cliente@email.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            service.register(new RegisterRequest(
                "Cliente", "cliente@email.com", "75999999999", "senha123"
            ))
        );

        verify(usuarios, never()).save(any());
        verify(identidades, never()).save(any());
    }

    @Test
    void deveAutenticarComSenhaLocal() {
        var service = new AuthService(usuarios, papeis, identidades, encoder, jwt);
        var papel = new Papel(PapelNome.CLIENTE);
        var usuario = new Usuario("Cliente", "cliente@email.com", "75999999999");
        usuario.adicionarPapel(papel);
        var identidade = IdentidadeAutenticacao.local(usuario, "cliente@email.com", "hash");

        when(usuarios.findByEmailIgnoreCase("cliente@email.com")).thenReturn(java.util.Optional.of(usuario));
        when(identidades.findByProvedorAndIdentificadorExterno(ProvedorAutenticacao.LOCAL, "cliente@email.com"))
            .thenReturn(java.util.Optional.of(identidade));
        when(encoder.matches("senha123", "hash")).thenReturn(true);
        when(jwt.generateToken("cliente@email.com")).thenReturn("jwt");
        when(jwt.getExpirationSeconds()).thenReturn(1800L);

        var response = service.login(new LoginRequest("cliente@email.com", "senha123"));

        assertEquals("jwt", response.token());
        verify(encoder).matches("senha123", "hash");
    }

    @Test
    void deveAtualizarTelefoneDoUsuarioAutenticado() {
        var service = new AuthService(usuarios, papeis, identidades, encoder, jwt);
        var papel = new Papel(PapelNome.CLIENTE);
        var usuario = new Usuario("Cliente", "cliente@email.com", null);
        usuario.adicionarPapel(papel);

        when(usuarios.findByEmailIgnoreCase("cliente@email.com")).thenReturn(java.util.Optional.of(usuario));

        var response = service.atualizarDados(
            "cliente@email.com",
            new AtualizarUsuarioRequest("75988887777")
        );

        assertEquals("75988887777", response.telefone());
        verify(usuarios).save(usuario);
    }

    @Test
    void deveRecusarSenhaLocalIncorreta() {
        var service = new AuthService(usuarios, papeis, identidades, encoder, jwt);
        var usuario = new Usuario("Cliente", "cliente@email.com", "75999999999");
        var identidade = IdentidadeAutenticacao.local(usuario, "cliente@email.com", "hash");

        when(usuarios.findByEmailIgnoreCase("cliente@email.com")).thenReturn(java.util.Optional.of(usuario));
        when(identidades.findByProvedorAndIdentificadorExterno(ProvedorAutenticacao.LOCAL, "cliente@email.com"))
            .thenReturn(java.util.Optional.of(identidade));
        when(encoder.matches("errada", "hash")).thenReturn(false);

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () ->
            service.login(new LoginRequest("cliente@email.com", "errada"))
        );
    }
}
