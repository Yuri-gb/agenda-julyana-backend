package br.com.agendajulyana.auth.security;

import br.com.agendajulyana.auth.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        var header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var token = header.substring(7);
            var email = jwtService.extractSubject(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuario -> {
                    if (usuario.getStatus().name().equals("ATIVO")) {
                        var authorities = usuario.getPapeis().stream()
                            .map(papel -> new SimpleGrantedAuthority("ROLE_" + papel.getNome().name()))
                            .toList();

                        var authentication = new UsernamePasswordAuthenticationToken(
                            usuario.getEmail(),
                            null,
                            authorities
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                });
            }
        } catch (RuntimeException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
