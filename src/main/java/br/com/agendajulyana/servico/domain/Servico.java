package br.com.agendajulyana.servico.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "servico")
public class Servico {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "imagem_url", length = 1000)
    private String imagemUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "servico_status")
    private ServicoStatus status;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "servico_categoria",
        joinColumns = @JoinColumn(name = "servico_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new HashSet<>();

    protected Servico() {
    }

    public Servico(String nome, String descricao, int duracaoMinutos, BigDecimal preco, String imagemUrl) {
        this.nome = nome;
        this.descricao = descricao;
        this.duracaoMinutos = duracaoMinutos;
        this.preco = preco;
        this.imagemUrl = imagemUrl;
        this.status = ServicoStatus.ATIVO;
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
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public BigDecimal getPreco() { return preco; }
    public String getImagemUrl() { return imagemUrl; }
    public ServicoStatus getStatus() { return status; }
    public Set<Categoria> getCategorias() { return categorias; }

    public void atualizar(String nome, String descricao, int duracaoMinutos, BigDecimal preco, String imagemUrl) {
        this.nome = nome;
        this.descricao = descricao;
        this.duracaoMinutos = duracaoMinutos;
        this.preco = preco;
        this.imagemUrl = imagemUrl;
    }

    public void alterarStatus(ServicoStatus status) {
        this.status = status;
    }

    public void substituirCategorias(Set<Categoria> categorias) {
        this.categorias.clear();
        this.categorias.addAll(categorias);
    }
}
