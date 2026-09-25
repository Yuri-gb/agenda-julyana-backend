package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.*;
import br.com.agendajulyana.auth.repository.*;
import br.com.agendajulyana.auth.security.JwtService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleAuthenticationService {
    private final UsuarioRepository usuarios;
    private final PapelRepository papeis;
    private final IdentidadeAutenticacaoRepository identidades;
    private final JwtService jwt;

    public GoogleAuthenticationService(UsuarioRepository usuarios, PapelRepository papeis,
                                       IdentidadeAutenticacaoRepository identidades, JwtService jwt) {
        this.usuarios = usuarios; this.papeis = papeis; this.identidades = identidades; this.jwt = jwt;
    }

    @Transactional
    public String authenticate(OAuth2User google) {
        String subject = google.getAttribute("sub");
        String email = google.getAttribute("email");
        String nome = google.getAttribute("name");
        Boolean verified = google instanceof OidcUser oidc
            ? oidc.getEmailVerified()
            : google.getAttribute("email_verified");

        if (subject == null || email == null || nome == null || !Boolean.TRUE.equals(verified)) {
            throw new IllegalStateException("Identidade Google inválida.");
        }

        var identidade = identidades.findByProvedorAndIdentificadorExterno(
            ProvedorAutenticacao.GOOGLE, subject).orElse(null);

        Usuario usuario;
        if (identidade != null) {
            usuario = identidade.getUsuario();
        } else {
            if (usuarios.existsByEmailIgnoreCase(email)) {
                throw new IllegalStateException(
                    "A conta já existe. Faça login normalmente e vincule o Google em um fluxo autenticado."
                );
            }
            usuario = new Usuario(nome, email.trim().toLowerCase(), null, null);
            usuario.adicionarPapel(papeis.findByNome(PapelNome.CLIENTE)
                .orElseThrow(() -> new IllegalStateException("Papel CLIENTE não configurado.")));
            usuarios.save(usuario);
            identidades.save(IdentidadeAutenticacao.google(usuario, subject));
        }

        if (usuario.getStatus() != UsuarioStatus.ATIVO) {
            throw new IllegalStateException("Conta bloqueada.");
        }
        return jwt.generateToken(usuario.getEmail());
    }
}
