package br.com.agendajulyana.auth.service;

import br.com.agendajulyana.auth.domain.*;
import br.com.agendajulyana.auth.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioRepository usuarios;
    private final PapelRepository papeis;
    private final IdentidadeAutenticacaoRepository identidades;
    private final PasswordEncoder encoder;

    @Value("${app.security.bootstrap-admin.enabled:false}")
    private boolean enabled;

    @Value("${app.security.bootstrap-admin.name:}")
    private String name;

    @Value("${app.security.bootstrap-admin.email:}")
    private String email;

    @Value("${app.security.bootstrap-admin.phone:}")
    private String phone;

    @Value("${app.security.bootstrap-admin.password:}")
    private String password;

    public AdminBootstrap(
        UsuarioRepository usuarios,
        PapelRepository papeis,
        IdentidadeAutenticacaoRepository identidades,
        PasswordEncoder encoder
    ) {
        this.usuarios = usuarios;
        this.papeis = papeis;
        this.identidades = identidades;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        if (usuarios.existsByPapelNome(PapelNome.ADMIN)) {
            log.info("Bootstrap de ADMIN ignorado: já existe administrador.");
            return;
        }

        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                "Bootstrap de ADMIN habilitado, mas nome, email, telefone e senha não foram configurados."
            );
        }

        var normalizedEmail = email.trim().toLowerCase();

        if (usuarios.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException(
                "Bootstrap de ADMIN não pode usar e-mail já cadastrado: " + normalizedEmail
            );
        }

        var papelAdmin = papeis.findByNome(PapelNome.ADMIN)
            .orElseThrow(() -> new IllegalStateException("Papel ADMIN não configurado."));

        var usuario = new Usuario(name.trim(), normalizedEmail, phone.trim());
        usuario.adicionarPapel(papelAdmin);
        usuarios.save(usuario);

        identidades.save(IdentidadeAutenticacao.local(
            usuario,
            normalizedEmail,
            encoder.encode(password)
        ));

        log.info("Administrador inicial criado pelo bootstrap: {}", normalizedEmail);
    }
}
