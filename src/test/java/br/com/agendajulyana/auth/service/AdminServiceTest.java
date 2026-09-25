package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.repository.AuditoriaRepository;
import br.com.agendajulyana.auth.domain.*;
import br.com.agendajulyana.auth.dto.CriarAdminRequest;
import br.com.agendajulyana.auth.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminServiceTest {
    @Mock UsuarioRepository usuarios; @Mock PapelRepository papeis; @Mock IdentidadeAutenticacaoRepository identidades;
    @Mock PasswordEncoder encoder; @Mock AuditoriaRepository auditorias;
    @Test void devePermitirAdminCriarOutroAdmin(){
        var service=new AdminService(usuarios,papeis,identidades,encoder,auditorias);
        var solicitante=new Usuario("Admin","admin@email.com","75999999999");solicitante.adicionarPapel(new Papel(PapelNome.ADMIN));
        var papelAdmin=new Papel(PapelNome.ADMIN);
        when(usuarios.findByEmailIgnoreCase("admin@email.com")).thenReturn(java.util.Optional.of(solicitante));
        when(usuarios.existsByEmailIgnoreCase("novo@email.com")).thenReturn(false);
        when(papeis.findByNome(PapelNome.ADMIN)).thenReturn(java.util.Optional.of(papelAdmin));
        when(encoder.encode("senha123")).thenReturn("hash");
        service.criarAdmin("admin@email.com",new CriarAdminRequest("Novo Admin","novo@email.com","75988887777","senha123"));
        verify(usuarios).save(any(Usuario.class));verify(identidades).save(any(IdentidadeAutenticacao.class));verify(auditorias).save(any());
    }
    @Test void deveBloquearClienteDeCriarAdmin(){
        var service=new AdminService(usuarios,papeis,identidades,encoder,auditorias);
        var solicitante=new Usuario("Cliente","cliente@email.com","75999999999");solicitante.adicionarPapel(new Papel(PapelNome.CLIENTE));
        when(usuarios.findByEmailIgnoreCase("cliente@email.com")).thenReturn(java.util.Optional.of(solicitante));
        assertThrows(ResponseStatusException.class,()->service.criarAdmin("cliente@email.com",new CriarAdminRequest("Novo Admin","novo@email.com","75988887777","senha123")));
        verify(usuarios,never()).save(any());verify(identidades,never()).save(any());
    }
}