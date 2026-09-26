package br.com.agendajulyana.disponibilidade.service;

import br.com.agendajulyana.auditoria.domain.Auditoria;
import br.com.agendajulyana.auditoria.repository.AuditoriaRepository;
import br.com.agendajulyana.disponibilidade.domain.Disponibilidade;
import br.com.agendajulyana.disponibilidade.dto.*;
import br.com.agendajulyana.disponibilidade.repository.DisponibilidadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Service
public class DisponibilidadeService {
    private final DisponibilidadeRepository repository;
    private final AuditoriaRepository auditorias;
    public DisponibilidadeService(DisponibilidadeRepository repository, AuditoriaRepository auditorias){this.repository=repository;this.auditorias=auditorias;}

    @Transactional public DisponibilidadeResponse criar(DisponibilidadeRequest r, UUID usuarioId){
        validarHorario(r.horaInicio(),r.horaFim());
        if(repository.existeSobreposicao(r.diaSemana().shortValue(),r.horaInicio(),r.horaFim(),null)) throw new IllegalArgumentException("Já existe disponibilidade sobreposta para esse dia.");
        var d=repository.save(new Disponibilidade(r.diaSemana().shortValue(),r.horaInicio(),r.horaFim()));
        auditar(usuarioId,"CRIAR_DISPONIBILIDADE",d.getId(),Map.of("diaSemana",r.diaSemana())); return toResponse(d);
    }
    @Transactional(readOnly=true) public List<DisponibilidadeResponse> listar(){
        return repository.findAll().stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly=true) public DisponibilidadeResponse buscar(UUID id){return toResponse(find(id));}
    @Transactional public DisponibilidadeResponse atualizar(UUID id, DisponibilidadeRequest r, UUID usuarioId){
        validarHorario(r.horaInicio(),r.horaFim()); Disponibilidade d=find(id);
        if(repository.existeSobreposicao(r.diaSemana().shortValue(),r.horaInicio(),r.horaFim(),id)) throw new IllegalArgumentException("Já existe disponibilidade sobreposta para esse dia."); d.atualizar(r.diaSemana().shortValue(),r.horaInicio(),r.horaFim()); auditar(usuarioId,"ATUALIZAR_DISPONIBILIDADE",id,Map.of("diaSemana",r.diaSemana())); return toResponse(d);
    }
    @Transactional public DisponibilidadeResponse alterarStatus(UUID id, DisponibilidadeStatusRequest r, UUID usuarioId){Disponibilidade d=find(id);d.alterarAtivo(r.ativo());auditar(usuarioId,"ALTERAR_STATUS_DISPONIBILIDADE",id,Map.of("ativo",r.ativo()));return toResponse(d);}
    private Disponibilidade find(UUID id){return repository.findById(id).orElseThrow(()->new EntityNotFoundException("Disponibilidade não encontrada."));}
    private void validarHorario(LocalTime inicio,LocalTime fim){if(!fim.isAfter(inicio))throw new IllegalArgumentException("horaFim deve ser posterior a horaInicio.");}
    private void auditar(UUID usuarioId,String acao,UUID id,Map<String,Object> contexto){auditorias.save(new Auditoria(usuarioId,acao,"DISPONIBILIDADE",id,"SUCESSO",contexto));}
    private DisponibilidadeResponse toResponse(Disponibilidade d){return new DisponibilidadeResponse(d.getId(),d.getDiaSemana().intValue(),d.getHoraInicio(),d.getHoraFim(),d.isAtivo());}
}