package br.com.agendajulyana.servico.service;

import br.com.agendajulyana.auditoria.domain.Auditoria;
import br.com.agendajulyana.auditoria.repository.AuditoriaRepository;
import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.servico.domain.*;
import br.com.agendajulyana.servico.dto.*;
import br.com.agendajulyana.servico.repository.CategoriaRepository;
import br.com.agendajulyana.servico.repository.ServicoRepository;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import org.mockito.*;
import java.math.BigDecimal; import java.util.*;
import static org.junit.jupiter.api.Assertions.*; import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ServicoServiceTest {
    @Mock ServicoRepository servicos; @Mock CategoriaRepository categorias; @Mock UsuarioRepository usuarios; @Mock AuditoriaRepository auditorias;
    @Test void deveCriarServicoComImagemECategorias(){
        var service=new ServicoService(servicos,categorias,usuarios,auditorias);var admin=new Usuario("Admin","admin@email.com","75999999999");
        var categoria=new Categoria("Massagem",null);var id=UUID.randomUUID();
        when(usuarios.findByEmailIgnoreCase("admin@email.com")).thenReturn(Optional.of(admin));when(servicos.existsByNomeIgnoreCase("Massagem Relaxante")).thenReturn(false);
        when(categorias.findAllById(Set.of(id))).thenReturn(List.of(categoria));when(servicos.save(any(Servico.class))).thenAnswer(i->i.getArgument(0));
        var response=service.criar("admin@email.com",new CriarServicoRequest("Massagem Relaxante","Descrição",60,new BigDecimal("120.00"),"https://exemplo/imagem.jpg",Set.of(id)));
        assertEquals("Massagem Relaxante",response.nome());assertEquals(new BigDecimal("120.00"),response.preco());assertEquals("https://exemplo/imagem.jpg",response.imagemUrl());assertEquals(1,response.categorias().size());verify(auditorias).save(any(Auditoria.class));
    }
    @Test void deveRejeitarNomeDuplicado(){
        var service=new ServicoService(servicos,categorias,usuarios,auditorias);var admin=new Usuario("Admin","admin@email.com","75999999999");
        when(usuarios.findByEmailIgnoreCase("admin@email.com")).thenReturn(Optional.of(admin));when(servicos.existsByNomeIgnoreCase("Massagem")).thenReturn(true);
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.criar("admin@email.com",new CriarServicoRequest("Massagem",null,60,new BigDecimal("100.00"),null,null)));verify(servicos,never()).save(any());
    }
    @Test void deveAlterarStatusSemApagarServico(){
        var service=new ServicoService(servicos,categorias,usuarios,auditorias);var admin=new Usuario("Admin","admin@email.com","75999999999");var servico=new Servico("Massagem",null,60,new BigDecimal("100.00"),null);
        when(usuarios.findByEmailIgnoreCase("admin@email.com")).thenReturn(Optional.of(admin));when(servicos.findById(any())).thenReturn(Optional.of(servico));
        var response=service.alterarStatus("admin@email.com",UUID.randomUUID(),new AlterarStatusServicoRequest(ServicoStatus.DESATIVADO));
        assertEquals(ServicoStatus.DESATIVADO,response.status());verify(servicos,never()).delete(any());
    }
}