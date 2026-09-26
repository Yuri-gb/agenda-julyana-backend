package br.com.agendajulyana.disponibilidade.service;

import br.com.agendajulyana.auditoria.domain.Auditoria;
import br.com.agendajulyana.auditoria.repository.AuditoriaRepository;
import br.com.agendajulyana.disponibilidade.domain.IndisponibilidadeServico;
import br.com.agendajulyana.disponibilidade.dto.*;
import br.com.agendajulyana.disponibilidade.repository.IndisponibilidadeServicoRepository;
import br.com.agendajulyana.servico.domain.Servico;
import br.com.agendajulyana.servico.repository.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class IndisponibilidadeServicoService {
    private final IndisponibilidadeServicoRepository repository; private final ServicoRepository servicoRepository; private final AuditoriaRepository auditorias;
    public IndisponibilidadeServicoService(IndisponibilidadeServicoRepository repository,ServicoRepository servicoRepository,AuditoriaRepository auditorias){this.repository=repository;this.servicoRepository=servicoRepository;this.auditorias=auditorias;}
    @Transactional public IndisponibilidadeServicoResponse criar(IndisponibilidadeServicoRequest r){
        validar(r.inicio(),r.fim()); if(repository.existeSobreposicao(r.servicoId(),r.inicio(),r.fim(),null)) throw new IllegalArgumentException("Já existe indisponibilidade sobreposta para esse serviço."); Servico s=servicoRepository.findById(r.servicoId()).orElseThrow(()->new EntityNotFoundException("Serviço não encontrado."));
        var x=repository.save(new IndisponibilidadeServico(s,r.inicio(),r.fim(),r.motivo())); auditorias.save(new Auditoria(null,"CRIAR_INDISPONIBILIDADE_SERVICO","INDISPONIBILIDADE_SERVICO",x.getId(),"SUCESSO",java.util.Map.of())); return toResponse(x);
    }
    @Transactional(readOnly=true) public List<IndisponibilidadeServicoResponse> listar(){return repository.findAll().stream().map(this::toResponse).toList();}
    @Transactional(readOnly=true) public IndisponibilidadeServicoResponse buscar(UUID id){return toResponse(find(id));}
    @Transactional public IndisponibilidadeServicoResponse atualizar(UUID id,IndisponibilidadeServicoRequest r){
        validar(r.inicio(),r.fim()); if(repository.existeSobreposicao(r.servicoId(),r.inicio(),r.fim(),id)) throw new IllegalArgumentException("Já existe indisponibilidade sobreposta para esse serviço."); if(!find(id).getServico().getId().equals(r.servicoId()))throw new IllegalArgumentException("O serviço da indisponibilidade não pode ser alterado.");
        IndisponibilidadeServico x=find(id);x.atualizar(r.inicio(),r.fim(),r.motivo());auditorias.save(new Auditoria(null,"ATUALIZAR_INDISPONIBILIDADE_SERVICO","INDISPONIBILIDADE_SERVICO",id,"SUCESSO",java.util.Map.of()));return toResponse(x);
    }
    @Transactional public void excluir(UUID id){repository.delete(find(id));}
    private IndisponibilidadeServico find(UUID id){return repository.findById(id).orElseThrow(()->new EntityNotFoundException("Indisponibilidade não encontrada."));}
    private void validar(OffsetDateTime i,OffsetDateTime f){if(!f.isAfter(i))throw new IllegalArgumentException("fim deve ser posterior a inicio.");}
    private IndisponibilidadeServicoResponse toResponse(IndisponibilidadeServico x){return new IndisponibilidadeServicoResponse(x.getId(),x.getServico().getId(),x.getInicio(),x.getFim(),x.getMotivo(),x.getCriadoEm());}
}