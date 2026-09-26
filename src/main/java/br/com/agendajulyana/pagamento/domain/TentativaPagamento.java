package br.com.agendajulyana.pagamento.domain;

import br.com.agendajulyana.agendamento.domain.ReservaTemporaria;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="tentativa_pagamento")
public class TentativaPagamento {
    @Id @GeneratedValue private UUID id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="reserva_id", nullable=false)
    private ReservaTemporaria reserva;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="pagamento_id", nullable=false)
    private Pagamento pagamento;

    @Column(name="referencia_provedor", length=255)
    private String referenciaProvedor;

    @Column(name="criado_em", nullable=false)
    private OffsetDateTime criadoEm;

    protected TentativaPagamento() {}

    public TentativaPagamento(ReservaTemporaria reserva, Pagamento pagamento, String referenciaProvedor) {
        this.reserva = reserva;
        this.pagamento = pagamento;
        this.referenciaProvedor = referenciaProvedor;
        this.criadoEm = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public ReservaTemporaria getReserva() { return reserva; }
    public Pagamento getPagamento() { return pagamento; }
    public String getReferenciaProvedor() { return referenciaProvedor; }
}
