package br.com.agendajulyana.disponibilidade.service;

import br.com.agendajulyana.disponibilidade.domain.Disponibilidade;
import br.com.agendajulyana.disponibilidade.dto.*;
import br.com.agendajulyana.disponibilidade.repository.DisponibilidadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class DisponibilidadeService {
    private final DisponibilidadeRepository repository;
    public DisponibilidadeService(DisponibilidadeRepository repository){this.repository=repository;}

    @Transactional public DisponibilidadeResponse criar(DisponibilidadeRequest r){
        validarHorario(r.horaInicio(),r.horaFim());
        return toResponse(repository.save(new Disponibilidade(r.diaSemana().shortValue(),r.horaInicio(),r.horaFim())));
    }
    @Transactional(readOnly=true) public List<DisponibilidadeResponse> listar(){
        return repository.findAll().stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly=true) public DisponibilidadeResponse buscar(UUID id){return toResponse(find(id));}
    @Transactional public DisponibilidadeResponse atualizar(UUID id, DisponibilidadeRequest r){
        validarHorario(r.horaInicio(),r.horaFim()); Disponibilidade d=find(id); d.atualizar(r.diaSemana().shortValue(),r.horaInicio(),r.horaFim()); return toResponse(d);
    }
    @Transactional public DisponibilidadeResponse alterarStatus(UUID id, DisponibilidadeStatusRequest r){Disponibilidade d=find(id);d.alterarAtivo(r.ativo());return toResponse(d);}
    private Disponibilidade find(UUID id){return repository.findById(id).orElseThrow(()->new EntityNotFoundException("Disponibilidade não encontrada."));}
    private void validarHorario(LocalTime inicio,LocalTime fim){if(!fim.isAfter(inicio))throw new IllegalArgumentException("horaFim deve ser posterior a horaInicio.");}
    private DisponibilidadeResponse toResponse(Disponibilidade d){return new DisponibilidadeResponse(d.getId(),d.getDiaSemana(),d.getHoraInicio(),d.getHoraFim(),d.isAtivo());}
}