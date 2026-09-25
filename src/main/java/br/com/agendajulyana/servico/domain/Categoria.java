package br.com.agendajulyana.servico.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    protected Categoria() {
    }

    public Categoria(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = true;
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
    public String getDescricao() { return descricao; }
    public boolean isAtivo() { return ativo; }

    public void atualizar(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public void alterarAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
