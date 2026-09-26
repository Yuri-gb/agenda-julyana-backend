package br.com.agendajulyana.disponibilidade.service;

import br.com.agendajulyana.auditoria.domain.Auditoria;
import br.com.agendajulyana.auditoria.repository.AuditoriaRepository;
import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.disponibilidade.domain.Bloqueio;
import br.com.agendajulyana.disponibilidade.dto.*;
import br.com.agendajulyana.disponibilidade.repository.BloqueioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BloqueioService {
    private final BloqueioRepository repository; private final UsuarioRepository usuarioRepository; private final AuditoriaRepository auditorias;
    public BloqueioService(BloqueioRepository repository,UsuarioRepository usuarioRepository,AuditoriaRepository auditorias){this.repository=repository;this.usuarioRepository=usuarioRepository;this.auditorias=auditorias;}
    @Transactional public BloqueioResponse criar(BloqueioRequest r, UUID usuarioId){
        validar(r.inicio(),r.fim()); if(repository.existeSobreposicao(r.inicio(),r.fim(),null)) throw new IllegalArgumentException("Já existe bloqueio sobreposto."); Usuario u=usuarioRepository.findById(usuarioId).orElseThrow(()->new EntityNotFoundException("Usuário não encontrado."));
        var b=repository.save(new Bloqueio(r.inicio(),r.fim(),r.motivo(),u)); auditorias.save(new Auditoria(usuarioId,"CRIAR_BLOQUEIO","BLOQUEIO",b.getId(),"SUCESSO",java.util.Map.of())); return toResponse(b);
    }
    @Transactional(readOnly=true) public List<BloqueioResponse> listar(){return repository.findAll().stream().map(this::toResponse).toList();}
    @Transactional(readOnly=true) public BloqueioResponse buscar(UUID id){return toResponse(find(id));}
    @Transactional public BloqueioResponse atualizar(UUID id,BloqueioRequest r){validar(r.inicio(),r.fim());if(repository.existeSobreposicao(r.inicio(),r.fim(),id)) throw new IllegalArgumentException("Já existe bloqueio sobreposto.");Bloqueio b=find(id);b.atualizar(r.inicio(),r.fim(),r.motivo());auditorias.save(new Auditoria(null,"ATUALIZAR_BLOQUEIO","BLOQUEIO",id,"SUCESSO",java.util.Map.of()));return toResponse(b);}
    @Transactional public void excluir(UUID id){repository.delete(find(id));}
    private Bloqueio find(UUID id){return repository.findById(id).orElseThrow(()->new EntityNotFoundException("Bloqueio não encontrado."));}
    private void validar(OffsetDateTime i,OffsetDateTime f){if(!f.isAfter(i))throw new IllegalArgumentException("fim deve ser posterior a inicio.");}
    private BloqueioResponse toResponse(Bloqueio b){return new BloqueioResponse(b.getId(),b.getInicio(),b.getFim(),b.getMotivo(),b.getCriadoPor().getId(),b.getCriadoEm());}
}