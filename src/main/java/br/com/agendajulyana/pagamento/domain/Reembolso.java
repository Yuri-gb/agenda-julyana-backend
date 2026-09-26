package br.com.agendajulyana.pagamento.domain;

import br.com.agendajulyana.agendamento.domain.Cancelamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="reembolso")
public class Reembolso {
    @Id @GeneratedValue private UUID id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="pagamento_id", nullable=false)
    private Pagamento pagamento;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="cancelamento_id")
    private Cancelamento cancelamento;

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(nullable=false, columnDefinition="reembolso_status")
    private ReembolsoStatus status;

    @Column(name="referencia_externa", length=255)
    private String referenciaExterna;

    @Column(name="solicitado_em", nullable=false)
    private OffsetDateTime solicitadoEm;

    @Column(name="processado_em")
    private OffsetDateTime processadoEm;

    private String motivo;

    protected Reembolso() {}

    public Reembolso(Pagamento pagamento, Cancelamento cancelamento, BigDecimal valor, String motivo) {
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("Valor de reembolso inválido.");
        this.pagamento=pagamento;
        this.cancelamento=cancelamento;
        this.valor=valor;
        this.status=ReembolsoStatus.SOLICITADO;
        this.solicitadoEm=OffsetDateTime.now();
        this.motivo=motivo;
    }

    public UUID getId(){return id;}
    public Pagamento getPagamento(){return pagamento;}
    public BigDecimal getValor(){return valor;}
    public ReembolsoStatus getStatus(){return status;}
    public String getReferenciaExterna(){return referenciaExterna;}
    public void marcarProcessando(){status=ReembolsoStatus.PROCESSANDO;}
    public void concluir(String referenciaExterna){status=ReembolsoStatus.CONCLUIDO;this.referenciaExterna=referenciaExterna;this.processadoEm=OffsetDateTime.now();}
    public void falhar(){status=ReembolsoStatus.FALHOU;this.processadoEm=OffsetDateTime.now();}
}
