package br.com.agendajulyana.auth.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "identidade_autenticacao",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_identidade_provedor_externo", columnNames = {"provedor", "identificador_externo"}),
        @UniqueConstraint(name = "uq_identidade_usuario_provedor", columnNames = {"usuario_id", "provedor"})
    })
public class IdentidadeAutenticacao {
    @Id @GeneratedValue private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProvedorAutenticacao provedor;

    @Column(name = "identificador_externo", nullable = false, length = 255)
    private String identificadorExterno;

    @Column(name = "senha_hash", length = 255)
    private String senhaHash;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    protected IdentidadeAutenticacao() {}

    private IdentidadeAutenticacao(Usuario usuario, ProvedorAutenticacao provedor,
                                   String identificadorExterno, String senhaHash) {
        this.usuario = usuario;
        this.provedor = provedor;
        this.identificadorExterno = identificadorExterno;
        this.senhaHash = senhaHash;
        this.criadoEm = OffsetDateTime.now();
        this.atualizadoEm = OffsetDateTime.now();
    }

    public static IdentidadeAutenticacao local(Usuario usuario, String email, String senhaHash) {
        return new IdentidadeAutenticacao(usuario, ProvedorAutenticacao.LOCAL,
            email.trim().toLowerCase(), senhaHash);
    }

    public static IdentidadeAutenticacao google(Usuario usuario, String subject) {
        return new IdentidadeAutenticacao(usuario, ProvedorAutenticacao.GOOGLE, subject, null);
    }

    public Usuario getUsuario() { return usuario; }
    public ProvedorAutenticacao getProvedor() { return provedor; }
    public String getIdentificadorExterno() { return identificadorExterno; }
    public String getSenhaHash() { return senhaHash; }
}
