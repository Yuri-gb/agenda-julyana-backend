package br.com.agendajulyana.disponibilidade.domain;

import br.com.agendajulyana.auth.domain.Usuario;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bloqueio")
public class Bloqueio {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private OffsetDateTime inicio;

    @Column(nullable = false)
    private OffsetDateTime fim;

    @Column(columnDefinition = "text")
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por", nullable = false)
    private Usuario criadoPor;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    protected Bloqueio() {}

    public Bloqueio(OffsetDateTime inicio, OffsetDateTime fim, String motivo, Usuario criadoPor) {
        this.inicio = inicio; this.fim = fim; this.motivo = motivo;
        this.criadoPor = criadoPor; this.criadoEm = OffsetDateTime.now();
    }

    public void atualizar(OffsetDateTime inicio, OffsetDateTime fim, String motivo) {
        this.inicio = inicio; this.fim = fim; this.motivo = motivo;
    }
    public UUID getId(){return id;} public OffsetDateTime getInicio(){return inicio;}
    public OffsetDateTime getFim(){return fim;} public String getMotivo(){return motivo;}
    public Usuario getCriadoPor(){return criadoPor;} public OffsetDateTime getCriadoEm(){return criadoEm;}
}