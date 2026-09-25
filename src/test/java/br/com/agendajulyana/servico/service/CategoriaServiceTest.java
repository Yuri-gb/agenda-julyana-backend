package br.com.agendajulyana.servico.service;

import br.com.agendajulyana.auth.domain.Auditoria;
import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.repository.AuditoriaRepository;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.servico.domain.Categoria;
import br.com.agendajulyana.servico.dto.CriarCategoriaRequest;
import br.com.agendajulyana.servico.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CategoriaServiceTest {

    @Mock CategoriaRepository categorias;
    @Mock UsuarioRepository usuarios;
    @Mock AuditoriaRepository auditorias;

    @Test
    void deveCriarCategoria() {
        var service = new CategoriaService(categorias, usuarios, auditorias);
        var admin = new Usuario("Admin", "admin@email.com", "75999999999");

        when(usuarios.findByEmailIgnoreCase("admin@email.com")).thenReturn(Optional.of(admin));
        when(categorias.existsByNomeIgnoreCase("Massagem")).thenReturn(false);
        when(categorias.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.criar("admin@email.com", new CriarCategoriaRequest("Massagem", null));

        assertEquals("Massagem", response.nome());
        assertTrue(response.ativo());
        verify(auditorias).save(any(Auditoria.class));
    }

    @Test
    void deveRejeitarCategoriaDuplicada() {
        var service = new CategoriaService(categorias, usuarios, auditorias);
        var admin = new Usuario("Admin", "admin@email.com", "75999999999");

        when(usuarios.findByEmailIgnoreCase("admin@email.com")).thenReturn(Optional.of(admin));
        when(categorias.existsByNomeIgnoreCase("Massagem")).thenReturn(true);

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () ->
            service.criar("admin@email.com", new CriarCategoriaRequest("Massagem", null))
        );
        verify(categorias, never()).save(any());
    }
}
