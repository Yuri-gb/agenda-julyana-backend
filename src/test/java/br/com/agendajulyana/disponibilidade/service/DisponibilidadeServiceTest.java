package br.com.agendajulyana.disponibilidade.service;

import br.com.agendajulyana.disponibilidade.domain.Disponibilidade;
import br.com.agendajulyana.disponibilidade.dto.*;
import br.com.agendajulyana.disponibilidade.repository.DisponibilidadeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTest {
    @Mock DisponibilidadeRepository repository;
    @InjectMocks DisponibilidadeService service;

    @Test void deveCriarDisponibilidade(){
        when(repository.save(any(Disponibilidade.class))).thenAnswer(i->i.getArgument(0));
        var r=service.criar(new DisponibilidadeRequest(1,LocalTime.of(9,0),LocalTime.of(12,0)));
        assertEquals(1,r.diaSemana()); assertEquals(LocalTime.of(9,0),r.horaInicio());
        verify(repository).save(any(Disponibilidade.class));
    }

    @Test void deveRejeitarHorarioInvalido(){
        assertThrows(IllegalArgumentException.class,()->service.criar(
            new DisponibilidadeRequest(1,LocalTime.of(12,0),LocalTime.of(9,0))));
        verify(repository,never()).save(any());
    }
}