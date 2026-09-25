package br.com.agendajulyana.servico.service;

import br.com.agendajulyana.auditoria.domain.Auditoria;
import br.com.agendajulyana.auditoria.repository.AuditoriaRepository;
import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.servico.domain.Categoria;
import br.com.agendajulyana.servico.dto.*;
import br.com.agendajulyana.servico.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class CategoriaService {
    private final CategoriaRepository categorias;private final UsuarioRepository usuarios;private final AuditoriaRepository auditorias;
    public CategoriaService(CategoriaRepository categorias,UsuarioRepository usuarios,AuditoriaRepository auditorias){this.categorias=categorias;this.usuarios=usuarios;this.auditorias=auditorias;}
    @Transactional public CategoriaResponse criar(String email,CriarCategoriaRequest r){var u=usuario(email);var nome=r.nome().trim();if(categorias.existsByNomeIgnoreCase(nome))throw new ResponseStatusException(HttpStatus.CONFLICT,"Já existe uma categoria com esse nome.");var c=categorias.save(new Categoria(nome,texto(r.descricao())));auditar(u,"CRIAR_CATEGORIA",c.getId(),Map.of("nome",nome));return resposta(c);}
    @Transactional(readOnly=true) public List<CategoriaResponse> listar(){return categorias.findAll().stream().map(this::resposta).toList();}
    @Transactional(readOnly=true) public CategoriaResponse buscar(UUID id){return resposta(categorias.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoria não encontrada.")));}
    @Transactional public CategoriaResponse atualizar(String email,UUID id,AtualizarCategoriaRequest r){var u=usuario(email);var c=categorias.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoria não encontrada."));var nome=r.nome().trim();if(categorias.existsByNomeIgnoreCaseAndIdNot(nome,id))throw new ResponseStatusException(HttpStatus.CONFLICT,"Já existe uma categoria com esse nome.");c.atualizar(nome,texto(r.descricao()));auditar(u,"ATUALIZAR_CATEGORIA",id,Map.of("nome",nome));return resposta(c);}
    @Transactional public CategoriaResponse alterarStatus(String email,UUID id,AlterarStatusCategoriaRequest r){var u=usuario(email);var c=categorias.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoria não encontrada."));c.alterarAtivo(r.ativo());auditar(u,"ALTERAR_STATUS_CATEGORIA",id,Map.of("ativo",r.ativo()));return resposta(c);}
    private Usuario usuario(String email){return usuarios.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"Usuário autenticado não encontrado."));}
    private CategoriaResponse resposta(Categoria c){return new CategoriaResponse(c.getId(),c.getNome(),c.getDescricao(),c.isAtivo());}
    private String texto(String v){return v==null||v.isBlank()?null:v.trim();}
    private void auditar(Usuario u,String acao,UUID id,Map<String,Object> contexto){auditorias.save(new Auditoria(u,acao,"CATEGORIA",id,"SUCESSO",contexto));}
}