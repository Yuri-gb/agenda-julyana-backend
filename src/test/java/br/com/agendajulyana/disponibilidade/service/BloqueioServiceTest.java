package br.com.agendajulyana.disponibilidade.service;

import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.disponibilidade.domain.Bloqueio;
import br.com.agendajulyana.disponibilidade.dto.BloqueioRequest;
import br.com.agendajulyana.disponibilidade.repository.BloqueioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloqueioServiceTest {
    @Mock BloqueioRepository repository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks BloqueioService service;

    @Test void deveCriarBloqueioComUsuarioResponsavel(){
        UUID id=UUID.randomUUID();
        Usuario usuario=mock(Usuario.class);
        when(usuarioRepository.findById(id)).thenReturn(java.util.Optional.of(usuario));
        when(repository.save(any(Bloqueio.class))).thenAnswer(i->i.getArgument(0));
        var inicio=OffsetDateTime.now(); var fim=inicio.plusHours(2);
        var r=service.criar(new BloqueioRequest(inicio,fim,"Férias"),id);
        assertEquals("Férias",r.motivo());
        verify(repository).save(any(Bloqueio.class));
    }

    @Test void deveRejeitarIntervaloInvalido(){
        UUID id=UUID.randomUUID(); var agora=OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class,()->service.criar(new BloqueioRequest(agora,agora,"x"),id));
        verifyNoInteractions(usuarioRepository,repository);
    }
}