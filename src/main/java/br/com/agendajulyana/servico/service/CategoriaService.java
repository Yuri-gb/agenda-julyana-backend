package br.com.agendajulyana.servico.service;

import br.com.agendajulyana.auth.domain.Auditoria;
import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.repository.AuditoriaRepository;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.servico.domain.Categoria;
import br.com.agendajulyana.servico.dto.*;
import br.com.agendajulyana.servico.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final UsuarioRepository usuarios;
    private final AuditoriaRepository auditorias;

    public CategoriaService(CategoriaRepository categorias, UsuarioRepository usuarios, AuditoriaRepository auditorias) {
        this.categorias = categorias;
        this.usuarios = usuarios;
        this.auditorias = auditorias;
    }

    @Transactional
    public CategoriaResponse criar(String email, CriarCategoriaRequest request) {
        var usuario = usuario(email);
        var nome = request.nome().trim();
        if (categorias.existsByNomeIgnoreCase(nome))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome.");
        var categoria = categorias.save(new Categoria(nome, texto(request.descricao())));
        auditar(usuario, "CRIAR_CATEGORIA", categoria.getId(), Map.of("nome", nome));
        return resposta(categoria);
    }

    @Transactional(readOnly = true)
    public java.util.List<CategoriaResponse> listar() {
        return categorias.findAll().stream().map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(UUID id) {
        return resposta(categorias.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada.")));
    }

    @Transactional
    public CategoriaResponse atualizar(String email, UUID id, AtualizarCategoriaRequest request) {
        var usuario = usuario(email);
        var categoria = categorias.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada."));
        var nome = request.nome().trim();
        if (categorias.existsByNomeIgnoreCaseAndIdNot(nome, id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome.");
        categoria.atualizar(nome, texto(request.descricao()));
        auditar(usuario, "ATUALIZAR_CATEGORIA", id, Map.of("nome", nome));
        return resposta(categoria);
    }

    @Transactional
    public CategoriaResponse alterarStatus(String email, UUID id, AlterarStatusCategoriaRequest request) {
        var usuario = usuario(email);
        var categoria = categorias.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada."));
        categoria.alterarAtivo(request.ativo());
        auditar(usuario, "ALTERAR_STATUS_CATEGORIA", id, Map.of("ativo", request.ativo()));
        return resposta(categoria);
    }

    private Usuario usuario(String email) {
        return usuarios.findByEmailIgnoreCase(email).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário autenticado não encontrado."));
    }
    private CategoriaResponse resposta(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao(), c.isAtivo());
    }
    private String texto(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private void auditar(Usuario u, String acao, UUID id, Map<String,Object> contexto) {
        auditorias.save(new Auditoria(u, acao, "CATEGORIA", id, "SUCESSO", contexto));
    }
}
