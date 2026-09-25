package br.com.agendajulyana.servico.service;

import br.com.agendajulyana.auth.domain.Auditoria;
import br.com.agendajulyana.auth.domain.Usuario;
import br.com.agendajulyana.auth.repository.AuditoriaRepository;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.servico.domain.Categoria;
import br.com.agendajulyana.servico.domain.Servico;
import br.com.agendajulyana.servico.domain.ServicoStatus;
import br.com.agendajulyana.servico.dto.*;
import br.com.agendajulyana.servico.repository.CategoriaRepository;
import br.com.agendajulyana.servico.repository.ServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServicoService {

    private final ServicoRepository servicos;
    private final CategoriaRepository categorias;
    private final UsuarioRepository usuarios;
    private final AuditoriaRepository auditorias;

    public ServicoService(ServicoRepository servicos, CategoriaRepository categorias,
                          UsuarioRepository usuarios, AuditoriaRepository auditorias) {
        this.servicos = servicos;
        this.categorias = categorias;
        this.usuarios = usuarios;
        this.auditorias = auditorias;
    }

    @Transactional
    public ServicoResponse criar(String emailSolicitante, CriarServicoRequest request) {
        var usuario = usuario(emailSolicitante);
        var nome = request.nome().trim();
        if (servicos.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um serviço com esse nome.");
        }
        var servico = new Servico(nome, texto(request.descricao()), request.duracaoMinutos(),
            request.preco(), texto(request.imagemUrl()));
        servico.substituirCategorias(buscarCategorias(request.categoriaIds()));
        servicos.save(servico);
        auditar(usuario, "CRIAR_SERVICO", servico.getId(), Map.of("nome", nome));
        return resposta(servico);
    }

    @Transactional(readOnly = true)
    public java.util.List<ServicoResponse> listar() {
        return servicos.findAll().stream().map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscar(UUID id) {
        return resposta(servicos.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado.")));
    }

    @Transactional
    public ServicoResponse atualizar(String emailSolicitante, UUID id, AtualizarServicoRequest request) {
        var usuario = usuario(emailSolicitante);
        var servico = servicos.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado."));
        var nome = request.nome().trim();
        if (servicos.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um serviço com esse nome.");
        }
        servico.atualizar(nome, texto(request.descricao()), request.duracaoMinutos(),
            request.preco(), texto(request.imagemUrl()));
        servico.substituirCategorias(buscarCategorias(request.categoriaIds()));
        auditar(usuario, "ATUALIZAR_SERVICO", id, Map.of("nome", nome));
        return resposta(servico);
    }

    @Transactional
    public ServicoResponse alterarStatus(String emailSolicitante, UUID id, AlterarStatusServicoRequest request) {
        var usuario = usuario(emailSolicitante);
        var servico = servicos.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado."));
        servico.alterarStatus(request.status());
        auditar(usuario, "ALTERAR_STATUS_SERVICO", id, Map.of("status", request.status().name()));
        return resposta(servico);
    }

    private Usuario usuario(String email) {
        return usuarios.findByEmailIgnoreCase(email).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário autenticado não encontrado."));
    }

    private Set<Categoria> buscarCategorias(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) return new LinkedHashSet<>();
        var encontradas = new LinkedHashSet<>(categorias.findAllById(ids));
        if (encontradas.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais categorias não foram encontradas.");
        }
        return encontradas;
    }

    private ServicoResponse resposta(Servico s) {
        return new ServicoResponse(s.getId(), s.getNome(), s.getDescricao(), s.getDuracaoMinutos(),
            s.getPreco(), s.getImagemUrl(), s.getStatus(),
            s.getCategorias().stream()
                .map(c -> new ServicoResponse.CategoriaResumo(c.getId(), c.getNome()))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private void auditar(Usuario usuario, String acao, UUID recursoId, Map<String, Object> contexto) {
        auditorias.save(new Auditoria(usuario, acao, "SERVICO", recursoId, "SUCESSO", contexto));
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
