package br.com.agendajulyana.agendamento.domain;

import br.com.agendajulyana.auth.domain.Cliente;
import br.com.agendajulyana.servico.domain.Servico;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="agendamento")
public class Agendamento {
 @Id @GeneratedValue private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="cliente_id",nullable=false) private Cliente cliente;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="servico_id",nullable=false) private Servico servico;
 @Column(nullable=false) private OffsetDateTime inicio;
 @Column(nullable=false) private OffsetDateTime fim;
 @Column(name="duracao_minutos",nullable=false) private int duracaoMinutos;
 @Column(name="valor_servico",nullable=false,precision=10,scale=2) private BigDecimal valorServico;
 @Column(name="valor_entrada",nullable=false,precision=10,scale=2) private BigDecimal valorEntrada;
 @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.NAMED_ENUM) @Column(nullable=false,columnDefinition="agendamento_status") private AgendamentoStatus status;
 @Column(name="criado_em",nullable=false) private OffsetDateTime criadoEm;
 @Column(name="atualizado_em",nullable=false) private OffsetDateTime atualizadoEm;
 protected Agendamento(){}
 public Agendamento(Cliente cliente,Servico servico,OffsetDateTime inicio,OffsetDateTime fim){
  this.cliente=cliente;this.servico=servico;this.inicio=inicio;this.fim=fim;
  this.duracaoMinutos=servico.getDuracaoMinutos();this.valorServico=servico.getPreco();
  this.valorEntrada=servico.getPreco().divide(BigDecimal.valueOf(2),2,java.math.RoundingMode.HALF_UP);
  this.status=AgendamentoStatus.AGUARDANDO_PAGAMENTO;this.criadoEm=OffsetDateTime.now();this.atualizadoEm=this.criadoEm;
 }
 @PrePersist void prePersist(){var n=OffsetDateTime.now();if(criadoEm==null)criadoEm=n;atualizadoEm=n;}
 @PreUpdate void preUpdate(){atualizadoEm=OffsetDateTime.now();}
 public UUID getId(){return id;} public Cliente getCliente(){return cliente;} public Servico getServico(){return servico;}
 public OffsetDateTime getInicio(){return inicio;} public OffsetDateTime getFim(){return fim;} public int getDuracaoMinutos(){return duracaoMinutos;}
 public BigDecimal getValorServico(){return valorServico;} public BigDecimal getValorEntrada(){return valorEntrada;} public AgendamentoStatus getStatus(){return status;}
 public void confirmar(){exigir(AgendamentoStatus.AGUARDANDO_PAGAMENTO);status=AgendamentoStatus.CONFIRMADO;}
 public void cancelar(){if(status==AgendamentoStatus.REALIZADO||status==AgendamentoStatus.CANCELADO)throw new IllegalStateException("Agendamento não pode ser cancelado.");status=AgendamentoStatus.CANCELADO;}
 public void realizar(){exigir(AgendamentoStatus.CONFIRMADO);status=AgendamentoStatus.REALIZADO;}
 public void marcarNaoComparecimento(){exigir(AgendamentoStatus.CONFIRMADO);status=AgendamentoStatus.NAO_COMPARECEU;}
 private void exigir(AgendamentoStatus esperado){if(status!=esperado)throw new IllegalStateException("Transição de status inválida.");}
 public void reagendar(OffsetDateTime novoInicio){if(status!=AgendamentoStatus.CONFIRMADO)throw new IllegalStateException("Apenas agendamento confirmado pode ser reagendado.");this.inicio=novoInicio;this.fim=novoInicio.plusMinutes(duracaoMinutos);}
}
