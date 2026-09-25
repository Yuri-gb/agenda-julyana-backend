package br.com.agendajulyana.auth.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "recuperacao_senha")
public class RecuperacaoSenha {
    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "codigo_hash", nullable = false, length = 255)
    private String codigoHash;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @Column(nullable = false)
    private int tentativas;

    @Column(name = "verificado_em")
    private OffsetDateTime verificadoEm;

    @Column(name = "token_redefinicao_hash", length = 64)
    private String tokenRedefinicaoHash;

    @Column(name = "token_redefinicao_expira_em")
    private OffsetDateTime tokenRedefinicaoExpiraEm;

    @Column(name = "utilizado_em")
    private OffsetDateTime utilizadoEm;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    protected RecuperacaoSenha() {}

    public RecuperacaoSenha(Usuario usuario, String codigoHash, OffsetDateTime expiraEm) {
        this.usuario = usuario;
        this.codigoHash = codigoHash;
        this.expiraEm = expiraEm;
        this.tentativas = 0;
        this.criadoEm = OffsetDateTime.now();
    }

    public Usuario getUsuario() { return usuario; }
    public String getCodigoHash() { return codigoHash; }
    public OffsetDateTime getExpiraEm() { return expiraEm; }
    public int getTentativas() { return tentativas; }
    public OffsetDateTime getVerificadoEm() { return verificadoEm; }
    public String getTokenRedefinicaoHash() { return tokenRedefinicaoHash; }
    public OffsetDateTime getTokenRedefinicaoExpiraEm() { return tokenRedefinicaoExpiraEm; }
    public OffsetDateTime getUtilizadoEm() { return utilizadoEm; }

    public void registrarTentativa() { this.tentativas++; }

    public void verificar(String tokenHash, OffsetDateTime tokenExpiraEm) {
        this.verificadoEm = OffsetDateTime.now();
        this.tokenRedefinicaoHash = tokenHash;
        this.tokenRedefinicaoExpiraEm = tokenExpiraEm;
    }

    public void utilizar() {
        this.utilizadoEm = OffsetDateTime.now();
    }
}
