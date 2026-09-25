package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.*;
import br.com.agendajulyana.auth.dto.*;
import br.com.agendajulyana.auth.repository.*;
import br.com.agendajulyana.auth.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PapelRepository papeis;
    private final IdentidadeAutenticacaoRepository identidades;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PapelRepository papeis,
                       IdentidadeAutenticacaoRepository identidades,
                       PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.papeis = papeis;
        this.identidades = identidades;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (usuarios.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        var usuario = new Usuario(request.nome().trim(), request.email().trim().toLowerCase(),
            request.telefone().trim());
        usuario.adicionarPapel(papeis.findByNome(PapelNome.CLIENTE)
            .orElseThrow(() -> new IllegalStateException("Papel CLIENTE não configurado.")));
        usuarios.save(usuario);
        identidades.save(IdentidadeAutenticacao.local(usuario, request.email(),
            encoder.encode(request.senha())));
        return response(usuario);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        var usuario = usuarios.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
        var identidade = identidades.findByProvedorAndIdentificadorExterno(
            ProvedorAutenticacao.LOCAL, request.email().trim().toLowerCase())
            .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
        if (usuario.getStatus() != UsuarioStatus.ATIVO ||
            !encoder.matches(request.senha(), identidade.getSenhaHash())) {
            throw new BadCredentialsException("Credenciais inválidas.");
        }
        return response(usuario);
    }

    @Transactional(readOnly = true)
    public MeResponse me(String email) {
        var usuario = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        var roles = usuario.getPapeis().stream().map(p -> p.getNome().name())
            .collect(Collectors.toUnmodifiableSet());
        return new MeResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
            usuario.getTelefone(), usuario.getStatus().name(), roles);
    }

    private LoginResponse response(Usuario usuario) {
        var roles = usuario.getPapeis().stream().map(p -> p.getNome().name())
            .collect(Collectors.toUnmodifiableSet());
        return new LoginResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
            roles, jwt.generateToken(usuario.getEmail()), jwt.getExpirationSeconds());
    }
}
