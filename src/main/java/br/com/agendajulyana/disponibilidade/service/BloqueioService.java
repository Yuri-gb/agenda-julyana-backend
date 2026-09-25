package br.com.agendajulyana.disponibilidade.service;

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
    private final BloqueioRepository repository; private final UsuarioRepository usuarioRepository;
    public BloqueioService(BloqueioRepository repository,UsuarioRepository usuarioRepository){this.repository=repository;this.usuarioRepository=usuarioRepository;}
    @Transactional public BloqueioResponse criar(BloqueioRequest r, UUID usuarioId){
        validar(r.inicio(),r.fim()); Usuario u=usuarioRepository.findById(usuarioId).orElseThrow(()->new EntityNotFoundException("Usuário não encontrado."));
        return toResponse(repository.save(new Bloqueio(r.inicio(),r.fim(),r.motivo(),u)));
    }
    @Transactional(readOnly=true) public List<BloqueioResponse> listar(){return repository.findAll().stream().map(this::toResponse).toList();}
    @Transactional(readOnly=true) public BloqueioResponse buscar(UUID id){return toResponse(find(id));}
    @Transactional public BloqueioResponse atualizar(UUID id,BloqueioRequest r){validar(r.inicio(),r.fim());Bloqueio b=find(id);b.atualizar(r.inicio(),r.fim(),r.motivo());return toResponse(b);}
    @Transactional public void excluir(UUID id){repository.delete(find(id));}
    private Bloqueio find(UUID id){return repository.findById(id).orElseThrow(()->new EntityNotFoundException("Bloqueio não encontrado."));}
    private void validar(OffsetDateTime i,OffsetDateTime f){if(!f.isAfter(i))throw new IllegalArgumentException("fim deve ser posterior a inicio.");}
    private BloqueioResponse toResponse(Bloqueio b){return new BloqueioResponse(b.getId(),b.getInicio(),b.getFim(),b.getMotivo(),b.getCriadoPor().getId(),b.getCriadoEm());}
}