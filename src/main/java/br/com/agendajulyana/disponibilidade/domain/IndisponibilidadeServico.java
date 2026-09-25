package br.com.agendajulyana.disponibilidade.domain;

import br.com.agendajulyana.servico.domain.Servico;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "indisponibilidade_servico")
public class IndisponibilidadeServico {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(nullable = false)
    private OffsetDateTime inicio;

    @Column(nullable = false)
    private OffsetDateTime fim;

    private String motivo;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    protected IndisponibilidadeServico() {}

    public IndisponibilidadeServico(Servico servico, OffsetDateTime inicio, OffsetDateTime fim, String motivo) {
        this.servico=servico; this.inicio=inicio; this.fim=fim; this.motivo=motivo; this.criadoEm=OffsetDateTime.now();
    }

    public void atualizar(OffsetDateTime inicio, OffsetDateTime fim, String motivo) {
        this.inicio=inicio; this.fim=fim; this.motivo=motivo;
    }
    public UUID getId(){return id;} public Servico getServico(){return servico;}
    public OffsetDateTime getInicio(){return inicio;} public OffsetDateTime getFim(){return fim;}
    public String getMotivo(){return motivo;} public OffsetDateTime getCriadoEm(){return criadoEm;}
}