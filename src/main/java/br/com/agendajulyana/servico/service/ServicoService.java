package br.com.agendajulyana.servico.service;

import br.com.agendajulyana.auditoria.domain.Auditoria;
import br.com.agendajulyana.auditoria.repository.AuditoriaRepository;
import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.servico.domain.Categoria;
import br.com.agendajulyana.servico.domain.Servico;
import br.com.agendajulyana.servico.dto.*;
import br.com.agendajulyana.servico.repository.CategoriaRepository;
import br.com.agendajulyana.servico.repository.ServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicoService {
    private final ServicoRepository servicos; private final CategoriaRepository categorias;
    private final UsuarioRepository usuarios; private final AuditoriaRepository auditorias;
    public ServicoService(ServicoRepository servicos,CategoriaRepository categorias,UsuarioRepository usuarios,AuditoriaRepository auditorias){
        this.servicos=servicos;this.categorias=categorias;this.usuarios=usuarios;this.auditorias=auditorias;
    }
    @Transactional public ServicoResponse criar(String email,CriarServicoRequest r){
        var u=usuario(email);var nome=r.nome().trim();
        if(servicos.existsByNomeIgnoreCase(nome))throw new ResponseStatusException(HttpStatus.CONFLICT,"Já existe um serviço com esse nome.");
        var s=new Servico(nome,texto(r.descricao()),r.duracaoMinutos(),r.preco(),texto(r.imagemUrl()));
        s.substituirCategorias(buscarCategorias(r.categoriaIds()));servicos.save(s);
        auditar(u,"CRIAR_SERVICO",s.getId(),Map.of("nome",nome));return resposta(s);
    }
    @Transactional(readOnly=true) public List<ServicoResponse> listar(){return servicos.findAll().stream().map(this::resposta).toList();}
    @Transactional(readOnly=true) public ServicoResponse buscar(UUID id){return resposta(servicos.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Serviço não encontrado.")));}
    @Transactional public ServicoResponse atualizar(String email,UUID id,AtualizarServicoRequest r){
        var u=usuario(email);var s=servicos.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Serviço não encontrado."));
        var nome=r.nome().trim();if(servicos.existsByNomeIgnoreCaseAndIdNot(nome,id))throw new ResponseStatusException(HttpStatus.CONFLICT,"Já existe um serviço com esse nome.");
        s.atualizar(nome,texto(r.descricao()),r.duracaoMinutos(),r.preco(),texto(r.imagemUrl()));s.substituirCategorias(buscarCategorias(r.categoriaIds()));
        auditar(u,"ATUALIZAR_SERVICO",id,Map.of("nome",nome));return resposta(s);
    }
    @Transactional public ServicoResponse alterarStatus(String email,UUID id,AlterarStatusServicoRequest r){
        var u=usuario(email);var s=servicos.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Serviço não encontrado."));
        s.alterarStatus(r.status());auditar(u,"ALTERAR_STATUS_SERVICO",id,Map.of("status",r.status().name()));return resposta(s);
    }
    private Usuario usuario(String email){return usuarios.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"Usuário autenticado não encontrado."));}
    private Set<Categoria> buscarCategorias(Set<UUID> ids){if(ids==null||ids.isEmpty())return new LinkedHashSet<>();var e=new LinkedHashSet<>(categorias.findAllById(ids));if(e.size()!=ids.size())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Uma ou mais categorias não foram encontradas.");return e;}
    private ServicoResponse resposta(Servico s){return new ServicoResponse(s.getId(),s.getNome(),s.getDescricao(),s.getDuracaoMinutos(),s.getPreco(),s.getImagemUrl(),s.getStatus(),s.getCategorias().stream().map(c->new ServicoResponse.CategoriaResumo(c.getId(),c.getNome())).collect(Collectors.toCollection(LinkedHashSet::new)));}
    private void auditar(Usuario u,String acao,UUID id,Map<String,Object> contexto){auditorias.save(new Auditoria(u.getId(),acao,"SERVICO",id,"SUCESSO",contexto));}
    private String texto(String v){return v==null||v.isBlank()?null:v.trim();}
}