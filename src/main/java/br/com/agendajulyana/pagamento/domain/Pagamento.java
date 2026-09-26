package br.com.agendajulyana.pagamento.domain;

import br.com.agendajulyana.agendamento.domain.Agendamento;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Agendamento agendamento;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "modalidade", nullable = false, columnDefinition = "pagamento_modalidade")
    private PagamentoModalidade modalidade;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "pagamento_status")
    private PagamentoStatus status;

    @Column(name = "provedor", nullable = false)
    private String provedor;

    @Column(name = "referencia_externa")
    private String referenciaExterna;

    @Column(name = "checkout_url")
    private String checkoutUrl;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    protected Pagamento() {}

    public Pagamento(Agendamento agendamento, PagamentoModalidade modalidade) {
        this.agendamento = agendamento;
        this.modalidade = modalidade;
        this.valor = modalidade == PagamentoModalidade.ENTRADA
                ? agendamento.getValorEntrada()
                : agendamento.getValorServico();
        this.status = PagamentoStatus.PENDENTE;
        this.provedor = "MERCADO_PAGO";
        this.criadoEm = OffsetDateTime.now();
        this.atualizadoEm = this.criadoEm;
    }

    @PrePersist
    void prePersist() {
        var agora = OffsetDateTime.now();
        if (criadoEm == null) criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public Agendamento getAgendamento() { return agendamento; }
    public BigDecimal getValor() { return valor; }
    public PagamentoModalidade getModalidade() { return modalidade; }
    public PagamentoStatus getStatus() { return status; }
    public String getProvedor() { return provedor; }
    public String getReferenciaExterna() { return referenciaExterna; }
    public String getCheckoutUrl() { return checkoutUrl; }

    public void registrarOrder(String referenciaExterna, String checkoutUrl) {
        exigir(PagamentoStatus.PENDENTE);
        if (referenciaExterna == null || referenciaExterna.isBlank()) {
            throw new IllegalArgumentException("A referência externa do pagamento é obrigatória.");
        }
        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalArgumentException("A URL de checkout do pagamento é obrigatória.");
        }
        this.referenciaExterna = referenciaExterna;
        this.checkoutUrl = checkoutUrl;
    }

    public void aprovar(String referenciaExterna) {
        exigir(PagamentoStatus.PENDENTE);
        this.status = PagamentoStatus.APROVADO;
        this.referenciaExterna = referenciaExterna;
    }

    public void recusar(String referenciaExterna) {
        exigir(PagamentoStatus.PENDENTE);
        this.status = PagamentoStatus.RECUSADO;
        this.referenciaExterna = referenciaExterna;
    }

    public void cancelar(String referenciaExterna) {
        if (status == PagamentoStatus.CANCELADO) {
            return;
        }
        this.status = PagamentoStatus.CANCELADO;
        this.referenciaExterna = referenciaExterna;
    }

    public BigDecimal valorReembolsoPorCancelamento(boolean canceladoPelaProfissional) {
        if (!canceladoPelaProfissional || status != PagamentoStatus.APROVADO) {
            return BigDecimal.ZERO.setScale(2);
        }
        return valor;
    }

    private void exigir(PagamentoStatus esperado) {
        if (status != esperado) {
            throw new IllegalStateException("Transição de pagamento inválida.");
        }
    }
}
