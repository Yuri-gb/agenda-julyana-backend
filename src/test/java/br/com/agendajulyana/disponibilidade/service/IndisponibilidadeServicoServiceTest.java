package br.com.agendajulyana.disponibilidade.service;

import br.com.agendajulyana.disponibilidade.domain.IndisponibilidadeServico;
import br.com.agendajulyana.disponibilidade.repository.IndisponibilidadeServicoRepository;
import br.com.agendajulyana.servico.domain.Servico;
import br.com.agendajulyana.servico.repository.ServicoRepository;
import br.com.agendajulyana.disponibilidade.dto.IndisponibilidadeServicoRequest;
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
class IndisponibilidadeServicoServiceTest {
    @Mock IndisponibilidadeServicoRepository repository;
    @Mock ServicoRepository servicoRepository;
    @InjectMocks IndisponibilidadeServicoService service;

    @Test void deveCriarIndisponibilidadeDoServico(){
        UUID id=UUID.randomUUID(); Servico servico=mock(Servico.class);
        when(servicoRepository.findById(id)).thenReturn(java.util.Optional.of(servico));
        when(repository.save(any(IndisponibilidadeServico.class))).thenAnswer(i->i.getArgument(0));
        var inicio=OffsetDateTime.now(); var fim=inicio.plusDays(1);
        var r=service.criar(new IndisponibilidadeServicoRequest(id,inicio,fim,"Manutenção"));
        assertEquals(id,r.servicoId()); assertEquals("Manutenção",r.motivo());
    }

    @Test void deveRejeitarIntervaloInvalido(){
        var agora=OffsetDateTime.now(); UUID id=UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,()->service.criar(new IndisponibilidadeServicoRequest(id,agora,agora,"x")));
        verifyNoInteractions(servicoRepository,repository);
    }
}