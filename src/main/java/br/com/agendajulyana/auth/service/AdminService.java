package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.*;
import br.com.agendajulyana.auth.dto.CriarAdminRequest;
import br.com.agendajulyana.auth.repository.IdentidadeAutenticacaoRepository;
import br.com.agendajulyana.auth.repository.PapelRepository;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AdminService {

    private final UsuarioRepository usuarios;
    private final PapelRepository papeis;
    private final IdentidadeAutenticacaoRepository identidades;
    private final PasswordEncoder encoder;
    private final AuditoriaRepository auditorias;

    public AdminService(
        UsuarioRepository usuarios,
        PapelRepository papeis,
        IdentidadeAutenticacaoRepository identidades,
        PasswordEncoder encoder,
        AuditoriaRepository auditorias
    ) {
        this.usuarios = usuarios;
        this.papeis = papeis;
        this.identidades = identidades;
        this.encoder = encoder;
        this.auditorias = auditorias;
    }

    @Transactional
    public void criarAdmin(String emailSolicitante, CriarAdminRequest request) {
        var solicitante = usuarios.findByEmailIgnoreCase(emailSolicitante)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Usuário autenticado não encontrado."
            ));

        var possuiAdmin = solicitante.getPapeis().stream()
            .anyMatch(papel -> papel.getNome() == PapelNome.ADMIN);

        if (!possuiAdmin) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Somente administradores podem criar administradores."
            );
        }

        var email = request.email().trim().toLowerCase();

        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        var usuario = new Usuario(
            request.nome().trim(),
            email,
            request.telefone().trim()
        );

        var papelAdmin = papeis.findByNome(PapelNome.ADMIN)
            .orElseThrow(() -> new IllegalStateException("Papel ADMIN não configurado."));

        usuario.adicionarPapel(papelAdmin);
        usuarios.save(usuario);

        identidades.save(IdentidadeAutenticacao.local(
            usuario,
            email,
            encoder.encode(request.senha())
        ));

        auditorias.save(new Auditoria(
            solicitante,
            "CRIAR_ADMIN",
            "USUARIO",
            usuario.getId(),
            "SUCESSO",
            Map.of("email", email)
        ));
    }
}
