package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.PapelNome;
import br.com.agendajulyana.auth.dto.LoginRequest;
import br.com.agendajulyana.auth.dto.LoginResponse;
import br.com.agendajulyana.auth.repository.UsuarioRepository;
import br.com.agendajulyana.auth.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        var usuario = usuarioRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));

        if (usuario.getStatus() != br.com.agendajulyana.auth.domain.UsuarioStatus.ATIVO
            || !passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("Credenciais inválidas.");
        }

        var token = jwtService.generateToken(usuario.getEmail());

        var papeis = usuario.getPapeis().stream()
            .map(papel -> papel.getNome().name())
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

        return new LoginResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            papeis,
            token,
            jwtService.getExpirationSeconds()
        );
    }
}
