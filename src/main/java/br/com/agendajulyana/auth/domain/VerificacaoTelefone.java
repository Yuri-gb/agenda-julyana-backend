package br.com.agendajulyana.auth.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "verificacao_telefone")
public class VerificacaoTelefone {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "codigo_hash", nullable = false, length = 64)
    private String codigoHash;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @Column(nullable = false)
    private int tentativas;

    @Column(name = "verificado_em")
    private OffsetDateTime verificadoEm;

    @Column(name = "utilizado_em")
    private OffsetDateTime utilizadoEm;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    protected VerificacaoTelefone() {}

    public VerificacaoTelefone(Usuario usuario, String codigoHash, OffsetDateTime expiraEm) {
        this.usuario = usuario;
        this.codigoHash = codigoHash;
        this.expiraEm = expiraEm;
        this.criadoEm = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getCodigoHash() { return codigoHash; }
    public OffsetDateTime getExpiraEm() { return expiraEm; }
    public int getTentativas() { return tentativas; }
    public OffsetDateTime getVerificadoEm() { return verificadoEm; }
    public OffsetDateTime getUtilizadoEm() { return utilizadoEm; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public void registrarTentativa() { tentativas++; }
    public void confirmar() {
        verificadoEm = OffsetDateTime.now();
        utilizadoEm = verificadoEm;
    }
    public void invalidar() { utilizadoEm = OffsetDateTime.now(); }
}
