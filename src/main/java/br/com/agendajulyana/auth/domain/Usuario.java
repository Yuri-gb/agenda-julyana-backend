package br.com.agendajulyana.auth.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 30)
    private String telefone;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioStatus status;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_papel",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "papel_id")
    )
    private Set<Papel> papeis = new HashSet<>();

    protected Usuario() {
    }

    public Usuario(String nome, String email, String telefone, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senhaHash = senhaHash;
        this.status = UsuarioStatus.ATIVO;
        this.criadoEm = OffsetDateTime.now();
        this.atualizadoEm = OffsetDateTime.now();
    }

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        criadoEm = now;
        atualizadoEm = now;
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public String getSenhaHash() { return senhaHash; }
    public UsuarioStatus getStatus() { return status; }
    public Set<Papel> getPapeis() { return papeis; }

    public void adicionarPapel(Papel papel) {
        papeis.add(papel);
    }
}
